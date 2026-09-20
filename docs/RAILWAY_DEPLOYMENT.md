# AbisheikMart Railway Deployment

This deployment keeps the existing Java Servlet/JSP architecture. Railway builds the Maven WAR in a multi-stage Docker build, runs the WAR on Apache Tomcat 9 with Java 17, and starts H2 in TCP Server Mode inside the same service. A Railway persistent volume stores the H2 database files.

## Required account access

The deployer must have a Railway account with permission to create a project, create a service from the GitHub repository `abisheik-cmd/AbisheikMart`, add a persistent volume, set service variables, and generate a public domain. GitHub repository access is also required so Railway can read the `main` branch.

No application credentials are required for the initial deployment. Railway's generated HTTPS hostname is used instead of a custom domain.

## Dashboard procedure

1. Sign in to Railway at <https://railway.com>.
2. Create a new project and choose **Deploy from GitHub repo**.
3. Select `abisheik-cmd/AbisheikMart`, branch `main`.
4. Let Railway detect the root `Dockerfile`; do not select a Node.js, Spring Boot, or managed database template.
5. Open the service **Settings** and confirm the build method is Dockerfile-based.
6. Add a persistent volume mounted at `/data`. The H2 database is stored under `/data/h2`.
7. Add these service variables. The values below are deployment defaults and contain no secret:

```text
DB_DRIVER=org.h2.Driver
DB_USERNAME=sa
DB_PASSWORD=
H2_PORT=9123
H2_DATA_DIR=/data/h2
DB_URL=jdbc:h2:tcp://127.0.0.1:9123//data/h2/abisheikmartdb;DB_CLOSE_DELAY=-1
```

The application reads `DB_URL`, `DB_DRIVER`, `DB_USERNAME`, and `DB_PASSWORD` in `AppContextListener`. Railway supplies `PORT`; the entrypoint rewrites Tomcat's HTTP connector to use it.

8. Deploy the service and wait for the Docker build and startup logs to show. The image deploys the WAR as Tomcat `ROOT.war`, so the generated hostname serves the application at `/`:

```text
Starting AbisheikMart on Tomcat ... with H2 TCP Server Mode
Server startup in ... milliseconds
AbisheikMart 2.0 Application Context initialized successfully.
```

9. In **Settings → Networking → Public Networking**, choose **Generate Domain**. The resulting `https://...up.railway.app` URL is the review URL.
10. Configure the service health check path as `/` if Railway does not import the value from `railway.toml` automatically.

## Remote verification checklist

After the domain is generated, verify these routes over HTTPS:

```text
/
/login
/register
/catalog
/products
/cart
/checkout
/orders
/seller/dashboard
/admin
/reviews
```

Expected unauthenticated behavior is HTTP 200 for public pages and a redirect to login for protected pages.

Use the seeded demonstration accounts only after confirming the deployed database is isolated for the review environment:

```text
Admin:  admin@abishmart.com / Admin@123
Seller: seller@abishmart.com / Seller@123
```

Then verify:

- Buyer: login → browse → product → add to cart → checkout → order history.
- Seller: login → seller dashboard → own products → seller orders.
- Admin: login → admin dashboard → users, products, and orders.
- Persistence: create or update a record, restart/redeploy the service, and confirm the record remains because `/data` is mounted as a persistent volume.

## Important security note

The seeded admin and seller passwords are demonstration credentials already present in the existing application. Change or remove them before any public production use beyond the capstone review. Do not commit production passwords or provider tokens to GitHub.

## Local preflight

From the repository root:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn -B clean test
mvn -B package
```

The Docker image performs its own Maven WAR build, so a local `target/` directory is not required for Railway.
