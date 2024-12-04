package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.CountryImportDto;
import softuni.exam.models.entity.Country;
import softuni.exam.repository.CountryRepository;
import softuni.exam.service.CountryService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class CountryServiceImpl implements CountryService {

    private static final String IMPORT_JSON_PATH = "src/main/resources/files/json/countries.json";

    private final CountryRepository countryRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;

    @Autowired
    public CountryServiceImpl(CountryRepository countryRepository, Gson gson,
                              ModelMapper modelMapper, ValidationUtil validationUtil) {
        this.countryRepository = countryRepository;
        this.gson = gson;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
    }

    @Override
    public boolean areImported() {
        return this.countryRepository.count() > 0;
    }

    @Override
    public String readCountriesFromFile() throws IOException {
        return Files.readString(Path.of(IMPORT_JSON_PATH));
    }

    @Override
    public String importCountries() throws IOException {
        StringBuilder sb = new StringBuilder();

        CountryImportDto[] countryImportDtos = this.gson.fromJson(
                readCountriesFromFile(), CountryImportDto[].class);

        for (CountryImportDto countryImportDto : countryImportDtos) {
            Optional<Country> country = this.countryRepository.findByName(countryImportDto.getName());
            if (!this.validationUtil.isValid(countryImportDto) || country.isPresent()) {
                sb.append("Invalid country").append(System.lineSeparator());
                continue;
            }

            Country mappedCountry = this.modelMapper.map(countryImportDto, Country.class);
            this.countryRepository.saveAndFlush(mappedCountry);

            sb.append(String.format("Successfully imported country %s - %s",
                            countryImportDto.getName(), countryImportDto.getCapital()))
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }
}