package integration;

import dao.CityDAO;
import dao.CountryDAO;
import domain.City;
import domain.Continent;
import domain.Country;
import domain.CountryLanguage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoHibernateIntegrationTest {
    private static SessionFactory sessionFactory;
    private CityDAO cityDAO;
    private CountryDAO countryDAO;

    @BeforeAll
    static void createSessionFactory() throws Exception {
        String jdbcUrl = "jdbc:h2:mem:world;DB_CLOSE_DELAY=-1";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS world");
        }

        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "org.h2.Driver");
        properties.put(Environment.URL, jdbcUrl);
        properties.put(Environment.USER, "sa");
        properties.put(Environment.PASS, "");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        properties.put(Environment.HBM2DDL_AUTO, "create-drop");

        sessionFactory = new Configuration()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .addProperties(properties)
                .buildSessionFactory();
    }

    @AfterAll
    static void closeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        inTransaction(session -> {
            session.createQuery("delete from CountryLanguage").executeUpdate();
            session.createQuery("delete from City").executeUpdate();
            session.createQuery("delete from Country").executeUpdate();
            return null;
        });

        Country country = new Country();
        country.setId(1);
        country.setCode("TST");
        country.setAlternativeCode("TS");
        country.setName("Testland");
        country.setContinent(Continent.EUROPE);
        country.setRegion("Test region");
        country.setPopulation(1000);
        country.setSurfaceArea(new BigDecimal("12.50"));
        country.setLanguages(new HashSet<>());

        inTransaction(session -> {
            session.persist(country);
            for (int i = 1; i <= 3; i++) {
                City city = new City();
                city.setName("Test city " + i);
                city.setCountry(country);
                city.setDistrict("District " + i);
                city.setPopulation(i * 100);
                session.persist(city);
            }

            CountryLanguage language = new CountryLanguage();
            language.setCountry(country);
            language.setLanguage("Testish");
            language.setIsOfficial(true);
            language.setPercentage(new BigDecimal("75.50"));
            session.persist(language);
            country.getLanguages().add(language);
            return null;
        });
    }

    @Test
    void cityDaoReturnsCorrectPageAndTotalCountFromDatabase() {
        List<City> page = inTransaction(session -> cityDAO.getItems(1, 1));
        int totalCount = inTransaction(session -> cityDAO.getTotalCount());

        assertEquals(1, page.size());
        assertEquals("Test city 2", page.getFirst().getName());
        assertEquals(3, totalCount);
    }

    @Test
    void cityDaoLoadsRequestedCityAndItsCountry() {
        City secondCity = inTransaction(session -> cityDAO.getItems(1, 1).getFirst());

        City loaded = inTransaction(session -> cityDAO.getById(secondCity.getId()));

        assertEquals("Test city 2", loaded.getName());
        assertNotNull(loaded.getCountry());
        assertEquals("Testland", loaded.getCountry().getName());
    }

    @Test
    void countryDaoReturnsCountriesWithTheirLanguages() {
        List<Country> countries = inTransaction(session -> countryDAO.getAll());

        assertEquals(1, countries.size());
        Country country = countries.getFirst();
        assertEquals("Testland", country.getName());
        Set<CountryLanguage> languages = country.getLanguages();
        assertEquals(1, languages.size());
        CountryLanguage language = languages.iterator().next();
        assertEquals("Testish", language.getLanguage());
        assertTrue(language.getIsOfficial());
        assertEquals(new BigDecimal("75.50"), language.getPercentage());
    }

    private static <T> T inTransaction(SessionWork<T> work) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            T result = work.execute(session);
            session.getTransaction().commit();
            return result;
        } catch (RuntimeException exception) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw exception;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @FunctionalInterface
    private interface SessionWork<T> {
        T execute(Session session);
    }
}
