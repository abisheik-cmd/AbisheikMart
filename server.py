import os
import re
import json
import sqlite3
import hashlib
import uuid
from http.server import HTTPServer, SimpleHTTPRequestHandler

DB_PATH = 'ecommerce.db'
SESSIONS = {}

def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def hash_password(password, salt):
    return hashlib.sha256((password + salt).encode('utf-8')).hexdigest()

def get_token_user(headers):
    auth = headers.get('Authorization', '')
    if auth.startswith('Bearer '):
        token = auth[7:].strip()
        return SESSIONS.get(token)
    return None

class AbisheikmartHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory='public', **kwargs)

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.end_headers()

    def send_json(self, data, status=200):
        body = json.dumps(data).encode('utf-8')
        self.send_response(status)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', str(len(body)))
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(body)

    def read_body(self):
        content_length = int(self.headers.get('Content-Length', 0))
        if content_length == 0:
            return {}
        raw = self.rfile.read(content_length).decode('utf-8')
        try:
            return json.loads(raw)
        except Exception:
            return {}

    def do_GET(self):
        path = self.path.split('?')[0]

        if path == '/api/health':
            return self.send_json({"status": "ok", "message": "Abisheikmart Server is running", "version": "1.0.0"})

        if path == '/api/auth/me':
            user = get_token_user(self.headers)
            if not user:
                return self.send_json({"error": "Unauthorized"}, 401)
            return self.send_json({"user": user})

        if path == '/api/products':
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("""
                SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at
                FROM products p JOIN users u ON p.seller_id = u.id
                ORDER BY p.id DESC;
            """)
            rows = [dict(r) for r in cursor.fetchall()]
            conn.close()
            return self.send_json({"products": rows})

        m_prod = re.match(r'^/api/products/(\d+)$', path)
        if m_prod:
            prod_id = int(m_prod.group(1))
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("""
                SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at
                FROM products p JOIN users u ON p.seller_id = u.id WHERE p.id = ?;
            """, (prod_id,))
            row = cursor.fetchone()
            conn.close()
            if not row:
                return self.send_json({"error": "Product not found"}, 404)
            return self.send_json({"product": dict(row)})

        if path == '/api/seller/products':
            user = get_token_user(self.headers)
            if not user or user['role'] not in ('SELLER', 'ADMIN'):
                return self.send_json({"error": "Unauthorized"}, 401)
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("""
                SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at
                FROM products p JOIN users u ON p.seller_id = u.id WHERE p.seller_id = ? ORDER BY p.id DESC;
            """, (user['id'],))
            rows = [dict(r) for r in cursor.fetchall()]
            conn.close()
            return self.send_json({"products": rows})

        if path == '/api/cart':
            user = get_token_user(self.headers)
            if not user:
                return self.send_json({"error": "Unauthorized"}, 401)
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("""
                SELECT c.id, c.buyer_id, c.product_id, p.name AS product_name, COALESCE(p.description, '') AS description,
                       p.price AS unit_price, p.stock AS available_stock, p.category, p.seller_id, u.name AS seller_name, c.quantity
                FROM cart_items c
                JOIN products p ON c.product_id = p.id
                JOIN users u ON p.seller_id = u.id
                WHERE c.buyer_id = ?;
            """, (user['id'],))
            rows = [dict(r) for r in cursor.fetchall()]
            total = sum(r['unit_price'] * r['quantity'] for r in rows)
            conn.close()
            return self.send_json({"items": rows, "total": total})

        if path == '/api/orders':
            user = get_token_user(self.headers)
            if not user:
                return self.send_json({"error": "Unauthorized"}, 401)
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("SELECT id, buyer_id, total_amount, status, created_at FROM orders WHERE buyer_id = ? ORDER BY id DESC;", (user['id'],))
            orders = [dict(r) for r in cursor.fetchall()]
            for o in orders:
                cursor.execute("SELECT product_name, quantity, unit_price, subtotal FROM order_items WHERE order_id = ?;", (o['id'],))
                o['items'] = [dict(item_r) for item_r in cursor.fetchall()]
            conn.close()
            return self.send_json({"orders": orders})

        # Static file fallback
        return super().do_GET()

    def do_POST(self):
        path = self.path.split('?')[0]
        body = self.read_body()

        if path == '/api/auth/register':
            name = body.get('name', '').strip()
            email = body.get('email', '').strip()
            password = body.get('password', '')
            role = body.get('role', 'BUYER').upper()
            if not name or not email or not password:
                return self.send_json({"error": "Name, email, and password required"}, 400)
            conn = get_db()
            cursor = conn.cursor()
            salt = str(uuid.uuid4())[:16]
            pwd_hash = hash_password(password, salt)
            try:
                cursor.execute("INSERT INTO users (name, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?);", (name, email, pwd_hash, salt, role))
                conn.commit()
                user_id = cursor.lastrowid
                conn.close()
                token = str(uuid.uuid4())
                user = {"id": user_id, "name": name, "email": email, "role": role}
                SESSIONS[token] = user
                return self.send_json({"token": token, "user": user}, 201)
            except sqlite3.IntegrityError:
                conn.close()
                return self.send_json({"error": "User with this email already exists"}, 400)

        if path == '/api/auth/login':
            email = body.get('email', '').strip()
            password = body.get('password', '')
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("SELECT id, name, email, password_hash, salt, role FROM users WHERE email = ? OR email LIKE ?;", (email, email))
            row = cursor.fetchone()
            conn.close()
            if not row:
                return self.send_json({"error": "Invalid credentials"}, 401)
            if hash_password(password, row['salt']) != row['password_hash']:
                return self.send_json({"error": "Invalid credentials"}, 401)
            token = str(uuid.uuid4())
            user = {"id": row['id'], "name": row['name'], "email": row['email'], "role": row['role']}
            SESSIONS[token] = user
            return self.send_json({"token": token, "user": user})

        if path == '/api/auth/logout':
            auth = self.headers.get('Authorization', '')
            if auth.startswith('Bearer '):
                SESSIONS.pop(auth[7:].strip(), None)
            return self.send_json({"message": "Logged out successfully"})

        if path == '/api/cart/add':
            user = get_token_user(self.headers)
            if not user:
                return self.send_json({"error": "Unauthorized"}, 401)
            product_id = body.get('product_id', 0)
            quantity = body.get('quantity', 1)
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("SELECT stock FROM products WHERE id = ?;", (product_id,))
            prod = cursor.fetchone()
            if not prod:
                conn.close()
                return self.send_json({"error": "Product not found"}, 404)
            cursor.execute("SELECT id, quantity FROM cart_items WHERE buyer_id = ? AND product_id = ?;", (user['id'], product_id))
            c_item = cursor.fetchone()
            if c_item:
                new_q = c_item['quantity'] + quantity
                cursor.execute("UPDATE cart_items SET quantity = ? WHERE id = ?;", (new_q, c_item['id']))
            else:
                cursor.execute("INSERT INTO cart_items (buyer_id, product_id, quantity) VALUES (?, ?, ?);", (user['id'], product_id, quantity))
            conn.commit()
            conn.close()
            return self.send_json({"message": "Item added to cart"})

        if path == '/api/seller/products':
            user = get_token_user(self.headers)
            if not user or user['role'] not in ('SELLER', 'ADMIN'):
                return self.send_json({"error": "Unauthorized"}, 401)
            name = body.get('name', '').strip()
            desc = body.get('description', '').strip()
            price = float(body.get('price', 0.0))
            stock = int(body.get('stock', 0))
            category = body.get('category', 'General').strip()
            image_url = body.get('image_url', '').strip()
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("INSERT INTO products (seller_id, name, description, price, stock, category, image_url) VALUES (?, ?, ?, ?, ?, ?, ?);",
                           (user['id'], name, desc, price, stock, category, image_url))
            conn.commit()
            prod_id = cursor.lastrowid
            cursor.execute("SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at FROM products p JOIN users u ON p.seller_id = u.id WHERE p.id = ?;", (prod_id,))
            prod = dict(cursor.fetchone())
            conn.close()
            return self.send_json({"message": "Product created successfully", "product": prod}, 201)

        if path == '/api/checkout':
            user = get_token_user(self.headers)
            if not user:
                return self.send_json({"error": "Unauthorized"}, 401)
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("""
                SELECT c.id, c.product_id, p.name, p.seller_id, p.price, p.stock, c.quantity
                FROM cart_items c JOIN products p ON c.product_id = p.id WHERE c.buyer_id = ?;
            """, (user['id'],))
            items = cursor.fetchall()
            if not items:
                conn.close()
                return self.send_json({"error": "Cart is empty"}, 400)
            total = sum(i['price'] * i['quantity'] for i in items)
            cursor.execute("INSERT INTO orders (buyer_id, total_amount, status) VALUES (?, ?, 'PLACED');", (user['id'], total))
            order_id = cursor.lastrowid
            order_items = []
            for i in items:
                subtotal = i['price'] * i['quantity']
                cursor.execute("INSERT INTO order_items (order_id, product_id, seller_id, product_name, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?);",
                               (order_id, i['product_id'], i['seller_id'], i['name'], i['quantity'], i['price'], subtotal))
                cursor.execute("UPDATE products SET stock = stock - ? WHERE id = ?;", (i['quantity'], i['product_id']))
                order_items.append({"product_id": i['product_id'], "product_name": i['name'], "quantity": i['quantity'], "unit_price": i['price'], "subtotal": subtotal})
            cursor.execute("DELETE FROM cart_items WHERE buyer_id = ?;", (user['id'],))
            conn.commit()
            conn.close()
            return self.send_json({"message": "Order placed successfully", "order": {"order_id": order_id, "buyer_id": user['id'], "total_amount": total, "status": "PLACED", "items": order_items}}, 201)

        return self.send_json({"error": "Not found"}, 404)

    def do_PUT(self):
        path = self.path.split('?')[0]
        body = self.read_body()

        if path == '/api/cart/update':
            user = get_token_user(self.headers)
            if not user:
                return self.send_json({"error": "Unauthorized"}, 401)
            product_id = body.get('product_id', 0)
            quantity = body.get('quantity', 0)
            conn = get_db()
            cursor = conn.cursor()
            if quantity <= 0:
                cursor.execute("DELETE FROM cart_items WHERE buyer_id = ? AND product_id = ?;", (user['id'], product_id))
            else:
                cursor.execute("UPDATE cart_items SET quantity = ? WHERE buyer_id = ? AND product_id = ?;", (quantity, user['id'], product_id))
            conn.commit()
            conn.close()
            return self.send_json({"message": "Cart updated"})

        m_seller_put = re.match(r'^/api/seller/products/(\d+)$', path)
        if m_seller_put:
            user = get_token_user(self.headers)
            if not user or user['role'] not in ('SELLER', 'ADMIN'):
                return self.send_json({"error": "Unauthorized"}, 401)
            prod_id = int(m_seller_put.group(1))
            name = body.get('name')
            desc = body.get('description')
            price = body.get('price')
            stock = body.get('stock')
            category = body.get('category')
            image_url = body.get('image_url')
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("UPDATE products SET name=?, description=?, price=?, stock=?, category=?, image_url=? WHERE id=? AND seller_id=?;",
                           (name, desc, price, stock, category, image_url, prod_id, user['id']))
            conn.commit()
            conn.close()
            return self.send_json({"message": "Product updated"})

        return self.send_json({"error": "Not found"}, 404)

    def do_DELETE(self):
        path = self.path.split('?')[0]
        m_cart_del = re.match(r'^/api/cart/remove/(\d+)$', path)
        if m_cart_del:
            user = get_token_user(self.headers)
            if not user:
                return self.send_json({"error": "Unauthorized"}, 401)
            prod_id = int(m_cart_del.group(1))
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("DELETE FROM cart_items WHERE buyer_id = ? AND product_id = ?;", (user['id'], prod_id))
            conn.commit()
            conn.close()
            return self.send_json({"message": "Item removed"})

        m_seller_del = re.match(r'^/api/seller/products/(\d+)$', path)
        if m_seller_del:
            user = get_token_user(self.headers)
            if not user or user['role'] not in ('SELLER', 'ADMIN'):
                return self.send_json({"error": "Unauthorized"}, 401)
            prod_id = int(m_seller_del.group(1))
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("DELETE FROM products WHERE id = ? AND seller_id = ?;", (prod_id, user['id']))
            conn.commit()
            conn.close()
            return self.send_json({"message": "Product deleted"})

        return self.send_json({"error": "Not found"}, 404)

if __name__ == '__main__':
    port = 8080
    print("==================================================")
    print("  Starting Abisheikmart Server on http://localhost:8080")
    print("==================================================")
    server = HTTPServer(('0.0.0.0', port), AbisheikmartHandler)
    server.serve_forever()
