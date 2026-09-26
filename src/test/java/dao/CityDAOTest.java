package dao;

import domain.City;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CityDAOTest {
    private SessionFactory sessionFactory;
    private Session session;
    private CityDAO cityDAO;

    @BeforeEach
    void setUp() {
        sessionFactory = mock(SessionFactory.class);
        session = mock(Session.class);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        cityDAO = new CityDAO(sessionFactory);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getItemsAppliesOffsetAndLimitAndReturnsQueryResults() {
        Query<City> query = mock(Query.class);
        List<City> expected = List.of(new City(), new City());
        when(session.createQuery("select c from City c", City.class)).thenReturn(query);
        when(query.setFirstResult(10)).thenReturn(query);
        when(query.setMaxResults(5)).thenReturn(query);
        when(query.list()).thenReturn(expected);

        List<City> result = cityDAO.getItems(10, 5);

        assertSame(expected, result);
        verify(query).setFirstResult(10);
        verify(query).setMaxResults(5);
        verify(query).list();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTotalCountReturnsExactCount() {
        Query<Long> query = mock(Query.class);
        when(session.createQuery("select count(c) from City c", Long.class)).thenReturn(query);
        when(query.uniqueResult()).thenReturn(42L);

        assertEquals(42, cityDAO.getTotalCount());
        verify(query).uniqueResult();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getByIdBindsIdAndReturnsCityWithCountryQueryResult() {
        Query<City> query = mock(Query.class);
        City expected = new City();
        when(session.createQuery(
                "select c from City c join fetch c.country where c.id = :ID", City.class)).thenReturn(query);
        when(query.setParameter("ID", 17)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(expected);

        assertSame(expected, cityDAO.getById(17));
        verify(query).setParameter("ID", 17);
        verify(query).getSingleResult();
    }
}
