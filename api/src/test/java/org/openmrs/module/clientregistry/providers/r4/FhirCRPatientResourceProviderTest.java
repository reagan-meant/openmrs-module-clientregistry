package org.openmrs.module.clientregistry.providers.r4;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.clientregistry.api.CRPatientService;

@RunWith(MockitoJUnitRunner.class)
public class FhirCRPatientResourceProviderTest {
    
    private static final String PATIENT_UUID = "01v312b1-cfv6-43ab-ae87-24070c801d1b";
    
    private static final String WRONG_PATIENT_UUID = "417312c1-cx56-b3ab-aeb7-41070b401d1p";
    
    private static final String NAME = "John";
    
    private static final String GIVEN_NAME = "Smith";
    
    private static final String FAMILY_NAME = "Doe";
    
    private static final String GENDER = "male";
    
    private static final String IDENTIFIER = "F10000";
    
    private static final String BIRTH_DATE = "1967-02-04";
    
    private static final String DEATH_DATE = "2020-12-10";
    
    private static final String CITY = "Los Angeles";
    
    private static final String STATE = "California";
    
    private static final String COUNTRY = "USA";
    
    private static final String POSTAL_CODE = "90210";
    
    private static final String LAST_UPDATED_DATE = "2020-12-15";
    
    @Mock
    private CRPatientService crService;
    
    private FhirCRPatientResourceProvider resourceProvider;
    
    private Patient patient;
    
    @Before
    public void setup() {
        resourceProvider = new FhirCRPatientResourceProvider();
        resourceProvider.setCrService(crService);
        
        // init patient
        HumanName name = new HumanName();
        name.addGiven(GIVEN_NAME);
        name.setFamily(FAMILY_NAME);
        name.addGiven(NAME);
        patient = new Patient();
        patient.setId(PATIENT_UUID);
        patient.addName(name);
        patient.setActive(true);
        patient.setBirthDate(parseDate(BIRTH_DATE));
        patient.setDeceased(new BooleanType(true));
        patient.setGender(Enumerations.AdministrativeGender.MALE);
    }
    
    private Date parseDate(String dateString) {
        Date parsed = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            parsed = formatter.parse(dateString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return parsed;
    }
    
}
