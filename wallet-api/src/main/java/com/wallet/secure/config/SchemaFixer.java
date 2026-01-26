package com.wallet.secure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔧 EJECUTANDO REPARACIÓN DE ESQUEMA DE BASE DE DATOS...");

        try {
            // 1. Intentar eliminar las Foreign Keys antiguas (si existen)
            try {
                jdbcTemplate.execute("ALTER TABLE insurances DROP FOREIGN KEY insurances_ibfk_1");
                System.out.println("✅ FK insurances_ibfk_1 eliminada.");
            } catch (Exception e) { System.out.println("ℹ️ FK insurances_ibfk_1 no existía o ya fue borrada."); }

            try {
                jdbcTemplate.execute("ALTER TABLE tickets DROP FOREIGN KEY FK4eqsebpimnjen0q46ja6fl2hl");
                System.out.println("✅ FK tickets eliminada.");
            } catch (Exception e) { System.out.println("ℹ️ FK tickets no existía o ya fue borrada."); }

            // 2. Corregir tipos de datos a BIGINT
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT");
            System.out.println("✅ Users.id convertido a BIGINT.");

            jdbcTemplate.execute("ALTER TABLE insurances MODIFY COLUMN user_id BIGINT NOT NULL");
            System.out.println("✅ Insurances.user_id convertido a BIGINT.");

            jdbcTemplate.execute("ALTER TABLE tickets MODIFY COLUMN user_id BIGINT");
            System.out.println("✅ Tickets.user_id convertido a BIGINT.");

            // 3. Recrear las Foreign Keys
            try {
                jdbcTemplate.execute("ALTER TABLE insurances ADD CONSTRAINT FK_insurances_users FOREIGN KEY (user_id) REFERENCES users(id)");
                System.out.println("✅ FK Insurances restaurada.");
            } catch (Exception e) { System.out.println("⚠️ No se pudo restaurar FK Insurances (quizás ya existe)."); }

            try {
                jdbcTemplate.execute("ALTER TABLE tickets ADD CONSTRAINT FK_tickets_users FOREIGN KEY (user_id) REFERENCES users(id)");
                System.out.println("✅ FK Tickets restaurada.");
            } catch (Exception e) { System.out.println("⚠️ No se pudo restaurar FK Tickets (quizás ya existe)."); }

            System.out.println("🎉 REPARACIÓN DE ESQUEMA COMPLETADA.");

        } catch (Exception e) {
            System.err.println("❌ ERROR FATAL REPARANDO ESQUEMA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
