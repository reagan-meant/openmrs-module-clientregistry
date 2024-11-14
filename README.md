# OpenMRS Client Registry Module

## Overview
The **OpenMRS Client Registry Module** enables integration with a FHIR-based Client Registry to facilitate patient search and registration. This module allows OpenMRS to communicate with external Client Registries that support the FHIR (Fast Healthcare Interoperability Resources) standard.

---

## Prerequisites

- **Java** 1.8 or higher
- **Maven** 2.x or higher

Ensure these dependencies are installed before building the module.

---

## Building from Source

1. Clone or download the repository to your local machine.
2. Navigate to the module's root directory in the terminal.
3. Run the following Maven command to compile and package the module:

    ```bash
    mvn package
    ```

This will generate the `.omod` file in the `omod/target/` directory.

---

## Installation

Once you have built the `.omod` file, follow these steps to install the module into OpenMRS:

### Option 1: Using the OpenMRS Admin Interface

1. Go to **Administration > Manage Modules** in the OpenMRS Admin UI.
2. Upload the generated `.omod` file and click **Install**.

### Option 2: Manual Installation

If the web upload method is disabled, you can manually drop the `.omod` file into your OpenMRS modules directory:

1. Copy the `.omod` file to the `~/.OpenMRS/modules/` directory on the server where OpenMRS is installed.
   
   > Note: `~/.OpenMRS` is the application data directory used by OpenMRS. The exact location of this directory depends on your installation.

2. Restart OpenMRS or Tomcat to load and activate the module.

---

## Configuration

Before using the module, configure the necessary variables. You can set them via the **Docker environment file** or the **OpenMRS runtime properties** file.

Below are the configuration settings that must be defined:

- `CLIENTREGISTRY_SERVERURL`: The base URL of the FHIR-based Client Registry. For example: `https://hapi.fhir.org/baseR4/`
- `CLIENTREGISTRY_USERNAME`: The username to authenticate to the Client Registry. Example: `admin`
- `CLIENTREGISTRY_PASSWORD`: The password to authenticate to the Client Registry. Example: `Admin123`
- `CLIENTREGISTRY_IDENTIFIERROOT`: The identifier root for the client registry. Example: `http://clientregistry.org/openmrs`

Example configuration:

```yaml
CLIENTREGISTRY_SERVERURL: "https://hapi.fhir.org/baseR4/"
CLIENTREGISTRY_USERNAME: "admin"
CLIENTREGISTRY_PASSWORD: "Admin123"
CLIENTREGISTRY_IDENTIFIERROOT: "http://clientregistry.org/openmrs"
