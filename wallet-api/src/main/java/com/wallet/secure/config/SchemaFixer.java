package com.wallet.secure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Componente (CommandLineRunner) que ejecuta reparaciones automáticas en el esquema
 * de la base de datos al inicio de la aplicación.
 * Útil para corregir tipos de datos y claves foráneas en despliegues.
 */
@Component
public class SchemaFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Ejecuta las sentencias SQL de reparación.
     *
     * @param args Argumentos de arranque.
     * @throws Exception Si ocurre un error SQL crítico.
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("INFO: Ejecutando reparación de esquema de base de datos...");

        try {
            // 1. Intentar eliminar las Foreign Keys antiguas (si existen)
            try {
                jdbcTemplate.execute("ALTER TABLE insurances DROP FOREIGN KEY insurances_ibfk_1");
                System.out.println("INFO: FK insurances_ibfk_1 eliminada.");
            } catch (Exception e) { System.out.println("INFO: FK insurances_ibfk_1 no existía o ya fue borrada."); }

            try {
                jdbcTemplate.execute("ALTER TABLE tickets DROP FOREIGN KEY FK4eqsebpimnjen0q46ja6fl2hl");
                System.out.println("INFO: FK tickets eliminada.");
            } catch (Exception e) { System.out.println("INFO: FK tickets no existía o ya fue borrada."); }

            // 2. Corregir tipos de datos a BIGINT
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT");
            System.out.println("INFO: Users.id convertido a BIGINT.");

            jdbcTemplate.execute("ALTER TABLE insurances MODIFY COLUMN user_id BIGINT NOT NULL");
            System.out.println("INFO: Insurances.user_id convertido a BIGINT.");

            jdbcTemplate.execute("ALTER TABLE tickets MODIFY COLUMN user_id BIGINT");
            System.out.println("INFO: Tickets.user_id convertido a BIGINT.");

            // 3. Recrear las Foreign Keys
            try {
                jdbcTemplate.execute("ALTER TABLE insurances ADD CONSTRAINT FK_insurances_users FOREIGN KEY (user_id) REFERENCES users(id)");
                System.out.println("INFO: FK Insurances restaurada.");
            } catch (Exception e) { System.out.println("WARN: No se pudo restaurar FK Insurances (quizás ya existe)."); }

            try {
                jdbcTemplate.execute("ALTER TABLE tickets ADD CONSTRAINT FK_tickets_users FOREIGN KEY (user_id) REFERENCES users(id)");
                System.out.println("INFO: FK Tickets restaurada.");
            } catch (Exception e) { System.out.println("WARN: No se pudo restaurar FK Tickets (quizás ya existe)."); }

            System.out.println("INFO: Reparación de esquema completada.");

        } catch (Exception e) {
            System.err.println("ERROR FATAL REPARANDO ESQUEMA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
