package dao;

import domain.Country;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountryDAOTest {
    private Session session;
    private CountryDAO countryDAO;

    @BeforeEach
    void setUp() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        session = mock(Session.class);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        countryDAO = new CountryDAO(sessionFactory);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllFetchesLanguagesAndReturnsCountries() {
        Query<Country> query = mock(Query.class);
        List<Country> expected = List.of(new Country());
        when(session.createQuery("select c from Country c join fetch c.languages", Country.class))
                .thenReturn(query);
        when(query.list()).thenReturn(expected);

        assertSame(expected, countryDAO.getAll());
        verify(query).list();
    }
}
