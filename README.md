# Bookstore Administration System

## Setup Guidelines

### a) Tested Hardware Configurations

- **Processors:** AMD RYZEN 7 6000 series, Intel Core i5/i7
- **RAM:** Minimum 8GB (16GB recommended for optimal performance)
- **Storage:** 500GB SSD or equivalent (minimum 5GB free space for project)
- **Network:** Stable internet connection for dependency downloads

### b) Tested Operating Systems

- Windows 11
- macOS Ventura
- Ubuntu 20.04+

### c) Required Software Versions

#### Core Requirements

- **Java Development Kit (JDK):** Version 21+ (JDK 22 tested and recommended)
- **MySQL Server:** Version 8.0+
- **Apache Maven:** Version 3.9.9

#### Development Tools

- **IDE:** Visual Studio Code, IntelliJ IDEA, or Eclipse
- **Version Control:** Git (for GitHub integration)
- **Database Management:** MySQL Workbench
- **Command Line:** Terminal or Command Prompt

### d) Application Configuration

#### 1. Update Database Configuration

Edit the file `src/main/resources/application.properties` and replace the brackets with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/{Database_name}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username={username}
spring.datasource.password={password_username}
```

#### 2. Verify Tool Installations

Run the following commands to verify all required tools are installed:

```bash
mvn --version
java --version
mysql --version
```

#### 3. Database Setup

Create the database and import the schema:

```bash
mysql -u {user_name} -p{user_password} -e "DROP DATABASE IF EXISTS bookstore; CREATE DATABASE bookstore;"
mysql -u {user_name} -p{user_password} bookstore < bookstore.sql
```

Verify the imported data:

```bash
mysql -u {user_name} -p{user_password} bookstore -e "SELECT COUNT(*) as users FROM users; SELECT COUNT(*) as books FROM books;"
```

#### 4. Build and Run Application

Navigate to the project directory and start the application:

```bash
cd {System_directory}\bookstoremanagementsystemversion2
taskkill /F /IM java.exe
mvnw.cmd clean install
mvn clean spring-boot:run
```

#### 5. Access Application

1. Open your web browser
2. Navigate to `http://localhost:8080`
3. Log in as **Admin** with the following credentials:
   - **Username:** `admin`
   - **Password:** `adminpass`

---

**Instructor:** Lecturer Ralf-Oliver Mevius
