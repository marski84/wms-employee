FROM liquibase/liquibase:4.25

# Definiujemy ścieżkę katalogu domowego
ENV HOME_DIR=/liquibase

# Całość operacji wykonać jako użytkownik liquibase
USER liquibase

# Utworzenie katalogu na sterowniki JDBC
RUN mkdir -p ${HOME_DIR}/jdbc

# Pobranie sterownika JDBC
ADD --chmod=644 https://jdbc.postgresql.org/download/postgresql-42.6.0.jar ${HOME_DIR}/jdbc/postgresql-42.6.0.jar

# Kopiowanie plików z odpowiednimi uprawnieniami
COPY --chown=liquibase:liquibase changelog ${HOME_DIR}/changelog
COPY --chown=liquibase:liquibase db.changelog-master.xml ${HOME_DIR}
COPY --chown=liquibase:liquibase liquibase-employee.properties ${HOME_DIR}/liquibase-employee.properties

# Ustawienie katalogu roboczego
WORKDIR ${HOME_DIR}