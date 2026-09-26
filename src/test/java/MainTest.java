import domain.City;
import domain.Continent;
import domain.Country;
import domain.CountryLanguage;
import org.junit.jupiter.api.Test;
import redis.CityCountry;
import redis.Language;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MainTest {
    @Test
    void transformDataCopiesCityCountryAndLanguageFields() {
        CountryLanguage countryLanguage = new CountryLanguage();
        countryLanguage.setLanguage("Ukrainian");
        countryLanguage.setIsOfficial(true);
        countryLanguage.setPercentage(new BigDecimal("98.75"));

        Country country = new Country();
        country.setCode("UKR");
        country.setAlternativeCode("UA");
        country.setName("Ukraine");
        country.setContinent(Continent.EUROPE);
        country.setRegion("Eastern Europe");
        country.setSurfaceArea(new BigDecimal("603500.00"));
        country.setPopulation(37000000);
        country.setLanguages(Set.of(countryLanguage));

        City city = new City();
        city.setId(7);
        city.setName("Kyiv");
        city.setDistrict("Kyiv City");
        city.setPopulation(2900000);
        city.setCountry(country);

        List<CityCountry> result = Main.transformData(List.of(city));

        assertEquals(1, result.size());
        CityCountry mapped = result.getFirst();
        assertEquals(7, mapped.getId());
        assertEquals("Kyiv", mapped.getName());
        assertEquals("Kyiv City", mapped.getDistrict());
        assertEquals(2900000, mapped.getPopulation());
        assertEquals("UKR", mapped.getCountryCode());
        assertEquals("UA", mapped.getAlternativeCountryCode());
        assertEquals("Ukraine", mapped.getCountryName());
        assertEquals(Continent.EUROPE, mapped.getContinent());
        assertEquals("Eastern Europe", mapped.getCountryRegion());
        assertEquals(new BigDecimal("603500.00"), mapped.getCountrySurfaceArea());
        assertEquals(37000000, mapped.getCountryPopulation());
        assertEquals(1, mapped.getLanguages().size());

        Language mappedLanguage = mapped.getLanguages().iterator().next();
        assertEquals("Ukrainian", mappedLanguage.getLanguage());
        assertEquals(true, mappedLanguage.getIsOfficial());
        assertEquals(new BigDecimal("98.75"), mappedLanguage.getPercentage());
        assertFalse(mapped.getLanguages().contains(countryLanguage));
    }
}
