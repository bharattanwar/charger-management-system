# OCPP Charger Management System

This project implements an OCPP (Open Charge Point Protocol) server for managing electric vehicle chargers. It uses Spring Boot for the backend and WebSocket communication for OCPP message exchange.

## Prerequisites

* **Java:** 11 or later
* **Maven:** For dependency management and building the project
* **MySQL:** Database for storing charger and transaction data
* **Python:** 3.6 or later (for the OCPP client script)
* **Websockets python library:** `pip install websockets`

## Getting Started

1.  **Clone the repository:**

    ```bash
    git clone <your-repository-url>
    cd <your-project-directory>
    ```

2.  **Configure the Database:**

    * Create a MySQL database named `charger_management_db` (or modify the database name in `application.properties`).
    * Update the `application.properties` file with your MySQL database credentials:

        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/charger_management_db?createDatabaseIfNotExist=true
        spring.datasource.username=<your-mysql-username>
        spring.datasource.password=<your-mysql-password>
        spring.jpa.hibernate.ddl-auto=update
        ```

3.  **Build the Spring Boot Application:**

    ```bash
    mvn clean install
    ```

4.  **Run the Spring Boot Application:**

    ```bash
    java -jar target/charger-management-system-0.0.1-SNAPSHOT.jar
    ```

5.  **Run the Python OCPP Client:**

    * Navigate to the directory containing the `ocpp_client.py` script.
    * Run the script:

        ```bash
        python ocpp_client.py
        ```

    * Note: The python script expects the spring boot app to be running on `ws://localhost:8080/ocpp?validToken`.

## Testing

### Manual Testing

* **Invalid JSON Messages:** Send malformed JSON messages from the Python client to verify error handling.
* **Invalid OCPP Actions:** Send unsupported OCPP actions.
* **Invalid Payloads:** Send messages with missing or incorrect payload fields.
* **Database Disconnection:** Temporarily disconnect the database to test error handling.
* **Network Interruptions:** Simulate network interruptions.
* **Multiple Chargers:** Run multiple Python client instances with different charger IDs.
* **Edge Cases:** Test with extreme values and long strings.

### Automated Testing

* **Unit Tests:** Write JUnit tests for the `OcppWebSocketHandler` class to test individual methods.
* **Integration Tests:** Use Spring Test to write integration tests that test the entire flow, including database interaction.

## Security

* **Authentication:**
    * The application implements basic token-based authentication. The python client must connect to the websocket endpoint with the query parameter `?validToken`.
    * In a production environment, use a more robust authentication mechanism, such as JWT or OAuth 2.0.
* **Input Validation:**
    * The `handleStartTransaction` method includes basic input validation to check for required fields and valid data types.
    * Implement comprehensive input validation for all OCPP message payloads.

## Notes

* The python client includes retry logic for dropped websocket connections.
* The Spring boot application uses SLF4j for logging.
* The application includes basic error codes for error responses.
* The database will be automatically created if it does not exist.
* The application will automatically update the database schema if the model changes.
* The application uses async database operations, to prevent blocking the main thread.
