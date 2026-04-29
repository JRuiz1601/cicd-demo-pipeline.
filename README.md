# Taller CI/CD - Jenkins, SonarQube, Trivy y Docker

Este repositorio contiene la entrega del taller de diseno y construccion de pipelines. Se trabajo sobre una aplicacion Java/Spring Boot y se configuro un flujo de CI/CD en Jenkins para compilar, probar, analizar calidad, construir una imagen Docker y validar seguridad antes del despliegue.

La aplicacion expone una pagina simple en `http://localhost/` con el titulo del taller y datos del contenedor en ejecucion.

## Herramientas usadas

- Jenkins ejecutandose en Docker.
- SonarQube local para analisis estatico.
- Trivy instalado en el agente Jenkins para escaneo de vulnerabilidades.
- Docker para construir y ejecutar la imagen de la aplicacion.
- Maven ejecutado desde contenedores, por lo que no es necesario instalar Java/Maven directamente en Windows.

## Flujo configurado

El `Jenkinsfile` define estas etapas:

1. `Checkout`: descarga el codigo desde GitHub usando `Pipeline script from SCM`.
2. `Build & Test`: ejecuta `mvn clean verify` y excluye la prueba Selenium porque requiere un contenedor adicional.
3. `Static Analysis (SonarQube)`: publica el analisis en SonarQube con el proyecto `mi-app`.
4. `Quality Gate (SonarQube)`: detiene el pipeline si la puerta de calidad falla.
5. `Docker Build`: construye la imagen `mi-app:latest`.
6. `Container Security Scan (Trivy)`: escanea la imagen con Trivy y falla si encuentra vulnerabilidades `CRITICAL`.
7. `Deploy`: despliega la imagen localmente en el puerto `80` cuando las validaciones pasan.

El bloque `post` limpia el workspace, elimina recursos Docker no usados y reporta si el pipeline fallo.

## Infraestructura local

Para levantar Jenkins y SonarQube:

```bash
cd jenkins
docker compose -f docker-compose.local.yml up -d --build
```

Servicios usados:

- Jenkins: `http://localhost:8080`
- SonarQube: `http://localhost:9000`

El contenedor de Jenkins se construyo con Docker CLI y Trivy para poder ejecutar `docker build`, `docker run` y `trivy image` desde el pipeline.

## Configuracion realizada en Jenkins

Plugins instalados o validados:

- Git
- Pipeline
- Docker / Docker Pipeline
- SonarQube Scanner for Jenkins
- Workspace Cleanup

Configuracion de SonarQube:

- Nombre del servidor en Jenkins: `SonarQube`
- URL interna: `http://sonarqube:9000`
- Token guardado como credencial secreta en Jenkins.
- Webhook en SonarQube: `http://jenkins:8080/sonarqube-webhook/`

El job de Jenkins se configuro como `Pipeline script from SCM`, apuntando al repositorio de GitHub y al archivo `Jenkinsfile`.

## Seguridad y gatekeeping

El pipeline tiene dos controles antes del despliegue:

- SonarQube bloquea el flujo si falla el `Quality Gate`.
- Trivy bloquea el flujo si encuentra vulnerabilidades criticas:

```bash
trivy image --severity CRITICAL --exit-code 1 mi-app:latest
```

Durante la ejecucion del taller, Trivy encontro vulnerabilidades criticas en dependencias Java dentro de `app.jar`, por lo que el despliegue se detuvo correctamente. Esto valida el comportamiento esperado del gate de seguridad.

## Validacion local de la aplicacion

Para compilar sin instalar Maven localmente:

```bash
docker run --rm -v "$PWD":/workspace -w /workspace maven:3.8.8-eclipse-temurin-11 mvn clean verify -DexcludedGroups=au.com.equifax.cicddemo.domain.SystemTest
```

Para construir y ejecutar la imagen:

```bash
docker build -t mi-app:latest .
docker rm -f mi-app || true
docker run -d --name mi-app -p 80:80 mi-app:latest
```

Luego abrir:

```text
http://localhost/
```

## Archivos principales modificados

- `Jenkinsfile`: definicion completa del pipeline.
- `Dockerfile`: imagen de ejecucion de la app en el puerto `80`.
- `jenkins/Dockerfile`: Jenkins con Docker CLI y Trivy.
- `jenkins/docker-compose.local.yml`: servicios locales de Jenkins y SonarQube.
- `src/main/java/au/com/equifax/cicddemo/controller/ApiController.java`: pagina de inicio personalizada para el taller.
- `pom.xml`: ajustes de Java/Jacoco para ejecutar el proyecto en el entorno actual.
