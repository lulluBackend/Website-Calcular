# Usando Java 17 (alpine é uma versão leve)
FROM eclipse-temurin:17-jdk-alpine

# Diretório de trabalho dentro do container
WORKDIR /app

# Copia o arquivo jar gerado pelo build
COPY target/*.jar app.jar

# Expõe a porta que sua aplicação usa (padrão Spring Boot)
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]