# CICD Demo - Jenkins, SonarQube, Trivy y Docker

Proyecto Java/Spring Boot usado para el ejercicio de diseño y construccion de pipelines. El flujo definido en `Jenkinsfile` compila, prueba, analiza calidad, escanea vulnerabilidades de contenedor y despliega la aplicacion localmente con Docker.

## Flujo del Pipeline

El pipeline declarativo ejecuta estas etapas:

1. `Checkout`: obtiene el codigo desde el repositorio configurado en Jenkins con `Pipeline script from SCM`.
2. `Build & Test`: ejecuta `mvn clean verify`, publica resultados JUnit y archiva el `.jar`.
3. `Static Analysis (SonarQube)`: envia el analisis a SonarQube con `mvn sonar:sonar`.
4. `Quality Gate (SonarQube)`: detiene el despliegue si falla la puerta de calidad.
5. `Docker Build`: construye la imagen `mi-app:latest`.
6. `Container Security Scan (Trivy)`: falla el pipeline si Trivy encuentra vulnerabilidades `CRITICAL`.
7. `Deploy`: en ramas `main` o `master`, despliega el contenedor con `docker run -d --name mi-app -p 80:80 mi-app:latest`.

El bloque `post` limpia recursos Docker no usados, borra el workspace y detiene el contenedor si la ejecucion falla.

## Levantar Jenkins y SonarQube

Desde la carpeta del proyecto:

```bash
cd jenkins
docker compose -f docker-compose.local.yml up -d --build
```

Servicios:

- Jenkins: http://localhost:8080
- SonarQube: http://localhost:9000

En SonarQube inicia sesion con `admin/admin`, cambia la clave cuando lo solicite y genera un token para Jenkins.

## Configuracion de Jenkins

Instala o valida estos plugins:

- Git
- Pipeline
- Docker
- SonarQube Scanner for Jenkins
- Workspace Cleanup

Configura SonarQube en Jenkins:

1. Ve a `Manage Jenkins > System > SonarQube servers`.
2. Agrega un servidor con nombre exacto `SonarQube`.
3. Usa URL `http://sonarqube:9000`.
4. Agrega el token de SonarQube como credencial secreta.

En SonarQube agrega un webhook en `Administration > Configuration > Webhooks`:

```text
http://jenkins:8080/sonarqube-webhook/
```

El `Jenkinsfile` usa la red Docker `jenkins_default`, creada por `docker compose` al levantar `jenkins/docker-compose.local.yml`. Si cambias el nombre del proyecto de Compose, actualiza la variable `DOCKER_NETWORK` del pipeline.

Crea el job:

1. `New Item > Pipeline`.
2. En `Pipeline`, selecciona `Pipeline script from SCM`.
3. SCM: `Git`.
4. Repository URL: URL de tu repositorio GitHub o ruta del repo local.
5. Branch: `*/master` o `*/main`.
6. Script Path: `Jenkinsfile`.

Como alternativa, puedes usar `jenkins/job-config.xml` como export base del job y ajustar la URL del repositorio si trabajas con un fork propio.

## Gatekeeping

Para que el despliegue falle por seguridad:

- En SonarQube, configura una Quality Gate que falle ante Security Hotspots o ratings de seguridad insuficientes.
- En Jenkins, la etapa `Quality Gate (SonarQube)` usa `waitForQualityGate abortPipeline: true`.
- Trivy se ejecuta dentro del agente Jenkins con `trivy image`, escanea paquetes del sistema operativo y dependencias Java de la imagen, y usa AWS ECR como mirror de Java DB para reducir fallos de descarga.

## Validacion Local

No necesitas instalar Java ni Maven en Windows. Para compilar y probar usa Maven desde Docker:

```bash
docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.8.8-eclipse-temurin-11 mvn clean verify -DexcludedGroups=au.com.equifax.cicddemo.domain.SystemTest
```

La prueba Selenium queda excluida del build principal porque requiere un contenedor adicional llamado `selenium`; para el taller se validan las pruebas unitarias e integracion del pipeline.

Construir imagen:

```bash
docker build -t mi-app:latest .
```

Ejecutar aplicacion:

```bash
docker rm -f mi-app || true
docker run -d --name mi-app -p 80:80 mi-app:latest
```

Validar:

```bash
curl http://localhost/
```

La respuesta incluye el mensaje:

```json
"message":"Pipeline Jenkins CI/CD actualizado con SonarQube y Trivy"
```

## Entregables Sugeridos

- Export del job de Jenkins o captura del XML/configuracion del job.
- Pantallazos de plugins, configuracion de SonarQube y job `Pipeline script from SCM`.
- Capturas de ejecucion del pipeline con etapas exitosas y gates aplicados.
- Captura de la aplicacion en `http://localhost/`.
- Codigo fuente modificado: `Jenkinsfile`, `Dockerfile`, `.dockerignore`, `README.md`, `jenkins/Dockerfile`, `jenkins/docker-compose.local.yml`, `jenkins/job-config.xml` y clases Java actualizadas.