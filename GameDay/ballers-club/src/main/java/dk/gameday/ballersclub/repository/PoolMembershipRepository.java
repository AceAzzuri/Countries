package dk.gameday.ballersclub.repository;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.model.Pool;
import dk.gameday.ballersclub.model.PoolMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PoolMembershipRepository extends JpaRepository<PoolMembership, Long> {

    boolean existsByPoolAndUser(Pool pool, AppUser user);

    @Query("""
            select membership
            from PoolMembership membership
            join fetch membership.pool pool
            join fetch pool.owner
            where membership.user = :user
            order by pool.createdAt desc
            """)
    List<PoolMembership> findByUserWithPool(AppUser user);

    @Query("""
            select membership
            from PoolMembership membership
            join fetch membership.user
            where membership.pool = :pool
            order by membership.joinedAt asc
            """)
    List<PoolMembership> findByPoolWithUser(Pool pool);
}
