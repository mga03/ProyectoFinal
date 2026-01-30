package com.wallet.secure.repository;

import com.wallet.secure.entity.Insurance;
import com.wallet.secure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de pólizas de seguro.
 * <p>
 * Permite realizar operaciones CRUD y búsquedas filtradas por usuario,
 * título y categoría, permitiendo paginación.
 * </p>
 */
@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long> {

    /**
     * Obtiene una página de seguros asociados a un usuario.
     *
     * @param user El usuario propietario de los seguros.
     * @param pageable Información de paginación.
     * @return Una página de seguros del usuario.
     */
    org.springframework.data.domain.Page<Insurance> findByUser(User user, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Busca seguros de un usuario que contengan un texto en el título (ignorando mayúsculas/minúsculas).
     *
     * @param user El usuario propietario.
     * @param title El texto a buscar en el título.
     * @return Lista de seguros que coinciden con el criterio.
     */
    List<Insurance> findByUserAndTitleContainingIgnoreCase(User user, String title);

    /**
     * Busca seguros de un usuario que pertenezcan a una categoría específica.
     *
     * @param user El usuario propietario.
     * @param category La categoría del seguro.
     * @return Lista de seguros de la categoría indicada.
     */
    List<Insurance> findByUserAndCategory(User user, String category);

    /**
     * Busca seguros de un usuario filtrando por título y categoría simultáneamente.
     *
     * @param user El usuario propietario.
     * @param title El texto a buscar en el título.
     * @param category La categoría del seguro.
     * @return Lista de seguros que cumplen ambos criterios.
     */
    List<Insurance> findByUserAndTitleContainingIgnoreCaseAndCategory(User user, String title, String category);
}
