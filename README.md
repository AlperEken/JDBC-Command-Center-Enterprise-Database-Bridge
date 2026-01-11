# JDBC Command Center: Enterprise Database Bridge

## Key Features

The JDBC Command Center is a robust console-based management system designed to serve as a high-integrity interface between business logic and relational database systems. The platform implements a dual-layered access model, distinguishing between Customer and Administrator roles to ensure secure and relevant data interaction. Customers are empowered to manage their own profiles and initiate orders, while Administrators possess comprehensive oversight, including the management of supplier registries, product inventories, and discount history tracking. A critical feature of the system is its sophisticated order confirmation workflow, which ensures that transactions are not only recorded but also verified for stock availability and administrative approval before finalization.

## Technical Implementation

This application is engineered in Java and utilizes the Java Database Connectivity (JDBC) API to establish a persistent bridge to a PostgreSQL environment. The technical core focuses on secure data handling by employing `PreparedStatement` for all dynamic queries, effectively neutralizing the risk of SQL injection attacks. To ensure strict data consistency, the system implements manual transaction control—disabling auto-commit during complex operations to allow for rollbacks in the event of partial failures during order creation. Additionally, the architecture emphasizes security and portability by externalizing database credentials into a `db.properties` configuration file, which is loaded at runtime to initialize the connection pool.

## Challenges & Reflection

One of the primary architectural challenges was managing the atomicity of multi-table operations, particularly during the order-to-inventory synchronization process. This required a deep dive into transaction isolation and the strategic use of `rollback()` to prevent "zombie" orders that lack corresponding inventory deductions. Another significant hurdle was the implementation of a scalable role-based menu system that maintains state across the application's lifecycle while providing clear exception handling for database connection drops. Reflecting on the design, the decision to decouple configuration from the source code proved vital, as it allows the system to transition between development and production environments without requiring code modifications, thus mirroring enterprise-grade software deployment practices.

## Getting Started

To initialize the JDBC Command Center on your system, ensure you have a PostgreSQL database and the JDK installed, then follow these steps:

```bash
# Ensure db.properties is configured with your database URL, user, and password

# Compile the source code including the JDBC driver in your classpath
javac src/Main.java

# Execute the application
java -cp .:postgresql-42.7.4.jar src/Main
```
*Author: Alper Eken & Samuel Schulze, Course: Database Technology Semester: Autumn 2024*
