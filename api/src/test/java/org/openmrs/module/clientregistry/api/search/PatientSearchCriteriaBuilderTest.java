package org.openmrs.module.clientregistry.api.search;

import java.lang.reflect.Field;
import java.util.List;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.junit.Test;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;

import ca.uhn.fhir.rest.gclient.ICriterion;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PatientSearchCriteriaBuilderTest {
	
	private final PatientSearchCriteriaBuilder builder = new PatientSearchCriteriaBuilder();
	
	@Test
	public void buildCriteria_shouldGenerateCriterionForName() throws NoSuchFieldException, IllegalAccessException {
		PatientSearchParams params = new PatientSearchParams();
		params.setName(createStringParam("Smith"));
		
		List<ICriterion<?>> criterions = builder.buildCriteria(params);
		
		assertNotNull(criterions);
		assertEquals(1, criterions.size());
		
		ICriterion<?> criterion = criterions.get(0);
		
		Field nameField = criterion.getClass().getDeclaredField("myName");
		nameField.setAccessible(true);
		String name = (String) nameField.get(criterion);
		assertEquals("name", name);
		
		Field valueField = criterion.getClass().getDeclaredField("myValue");
		valueField.setAccessible(true);
		String value = (String) valueField.get(criterion);
		assertEquals("Smith", value);
	}
	
	@Test
	public void buildCriteria_shouldGenerateCriterionForGivenName() throws NoSuchFieldException, IllegalAccessException {
		PatientSearchParams params = new PatientSearchParams();
		params.setGiven(createStringParam("John"));
		List<ICriterion<?>> criterions = builder.buildCriteria(params);
		
		assertNotNull(criterions);
		assertEquals(1, criterions.size());
		
		ICriterion<?> criterion = criterions.get(0);
		
		Field nameField = criterion.getClass().getDeclaredField("myName");
		nameField.setAccessible(true);
		String name = (String) nameField.get(criterion);
		assertEquals("given", name);
		
		Field valueField = criterion.getClass().getDeclaredField("myValue");
		valueField.setAccessible(true);
		String value = (String) valueField.get(criterion);
		assertEquals("John", value);
	}
	
	@Test
	public void buildCriteria_shouldGenerateCriterionForFamilyName() throws NoSuchFieldException, IllegalAccessException {
		PatientSearchParams params = new PatientSearchParams();
		params.setFamily(createStringParam("Walter"));
		List<ICriterion<?>> criterions = builder.buildCriteria(params);
		
		assertNotNull(criterions);
		assertEquals(1, criterions.size());
		
		ICriterion<?> criterion = criterions.get(0);
		
		Field nameField = criterion.getClass().getDeclaredField("myName");
		nameField.setAccessible(true);
		String name = (String) nameField.get(criterion);
		assertEquals("family", name);
		
		Field valueField = criterion.getClass().getDeclaredField("myValue");
		valueField.setAccessible(true);
		String value = (String) valueField.get(criterion);
		assertEquals("Walter", value);
	}
	
	@Test
	public void buildCriteria_shouldGenerateCriterionForGender() throws NoSuchFieldException, IllegalAccessException {
		PatientSearchParams params = new PatientSearchParams();
		params.setGender(createTokenParam("male"));
		List<ICriterion<?>> criterions = builder.buildCriteria(params);
		
		assertNotNull(criterions);
		assertEquals(1, criterions.size());
		
		ICriterion<?> criterion = criterions.get(0);
		
		Field nameField = criterion.getClass().getDeclaredField("myName");
		nameField.setAccessible(true);
		String name = (String) nameField.get(criterion);
		assertEquals("gender", name);
		
		Field valueField = criterion.getClass().getDeclaredField("myValue");
		valueField.setAccessible(true);
		String value = (String) valueField.get(criterion);
		assertEquals("male", value);
	}
	
	@Test
	public void buildCriteria_shouldGenerateCriterionForIdentifier() throws NoSuchFieldException, IllegalAccessException {
		PatientSearchParams params = new PatientSearchParams();
		params.setIdentifier(createTokenParam("3r34g346-tk"));
		List<ICriterion<?>> criterions = builder.buildCriteria(params);
		
		assertNotNull(criterions);
		assertEquals(1, criterions.size());
		
		ICriterion<?> criterion = criterions.get(0);
		
		Field nameField = criterion.getClass().getDeclaredField("myName");
		nameField.setAccessible(true);
		String name = (String) nameField.get(criterion);
		assertEquals("identifier", name);
		
		Field valueField = criterion.getClass().getDeclaredField("myValue");
		valueField.setAccessible(true);
		String value = (String) valueField.get(criterion);
		assertEquals("3r34g346-tk", value);
	}
	
	@Test
	public void buildCriteria_shouldGenerateCriterionForPatientWhenPatientBirthDateIsProvided() throws NoSuchFieldException,
	        IllegalAccessException {
		PatientSearchParams params = new PatientSearchParams();
		params.setBirthDate(createDateRangeParam("1996-12-12"));
		List<ICriterion<?>> criterions = builder.buildCriteria(params);
		
		assertNotNull(criterions);
		assertEquals(2, criterions.size());
		
		ICriterion<?> criterion = criterions.get(0);
		
		Field nameField = criterion.getClass().getDeclaredField("myName");
		nameField.setAccessible(true);
		String name = (String) nameField.get(criterion);
		assertEquals("birthdate", name);
		
		Field valueField = criterion.getClass().getDeclaredField("myValue");
		valueField.setAccessible(true);
		String value = (String) valueField.get(criterion);
		assertTrue(value.endsWith("1996-12-12"));
	}
	
	@Test
	public void buildCriteria_shouldGenerateCriterionForBoolean() throws NoSuchFieldException, IllegalAccessException {
		PatientSearchParams params = new PatientSearchParams();
		params.setDeceased(createTokenParam("false"));
		List<ICriterion<?>> criterions = builder.buildCriteria(params);
		
		assertNotNull(criterions);
		assertEquals(1, criterions.size());
		
		ICriterion<?> criterion = criterions.get(0);
		
		Field nameField = criterion.getClass().getDeclaredField("myName");
		nameField.setAccessible(true);
		String name = (String) nameField.get(criterion);
		assertEquals("deceased", name);
		
		Field valueField = criterion.getClass().getDeclaredField("myValue");
		valueField.setAccessible(true);
		String value = (String) valueField.get(criterion);
		assertEquals("false", value);
	}
	
	@Test
	public void buildCriteria_shouldGenerateCriterionForAddressWhenCityIsProvided() throws NoSuchFieldException,
	        IllegalAccessException {
		PatientSearchParams params = new PatientSearchParams();
		params.setCity(createStringParam("Washington"));
		List<ICriterion<?>> criterions = builder.buildCriteria(params);
		
		assertNotNull(criterions);
		assertEquals(1, criterions.size());
		
		ICriterion<?> criterion = criterions.get(0);
		
		Field nameField = criterion.getClass().getDeclaredField("myName");
		nameField.setAccessible(true);
		String name = (String) nameField.get(criterion);
		assertEquals("address-city", name);
		
		Field valueField = criterion.getClass().getDeclaredField("myValue");
		valueField.setAccessible(true);
		String value = (String) valueField.get(criterion);
		assertEquals("Washington", value);
	}
	
	private StringAndListParam createStringParam(String paramValue) {
		return new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(paramValue)));
	}
	
	private TokenAndListParam createTokenParam(String paramValue) {
		return new TokenAndListParam().addAnd(new TokenOrListParam().add(new TokenParam(paramValue)));
	}
	
	private DateRangeParam createDateRangeParam(String paramValue) {
		return new DateRangeParam().setLowerBound(paramValue).setUpperBound(paramValue);
	}
}
