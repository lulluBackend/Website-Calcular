# Estágio 1: Build da aplicação
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia os arquivos do projeto
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn ./.mvn

# Garante que o mvnw tenha permissão de execução
RUN chmod +x mvnw

# Faz o build do projeto
RUN ./mvnw clean package -DskipTests

# Estágio 2: Imagem final mais leve
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]