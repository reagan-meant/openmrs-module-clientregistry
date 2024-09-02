package org.openmrs.module.clientregistry.api.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.springframework.stereotype.Component;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;

@Component
public class PatientSearchCriteriaBuilder {
	
	private static final Map<String, String> customPropertyToFhirMap;
	
	static {
		customPropertyToFhirMap = new HashMap<>();
		customPropertyToFhirMap.put("name.property", "name");
		customPropertyToFhirMap.put("given.property", "given");
		customPropertyToFhirMap.put("family.property", "family");
		customPropertyToFhirMap.put("city.property", "address-city");
		customPropertyToFhirMap.put("state.property", "address-state");
		customPropertyToFhirMap.put("postalCode.property", "address-postalcode");
		customPropertyToFhirMap.put("country.property", "address-country");
		customPropertyToFhirMap.put("_id.property", "_id");
		customPropertyToFhirMap.put("_lastUpdated.property", "_lastUpdated");
	}
	
	public List<ICriterion<?>> buildCriteria(PatientSearchParams patientSearchParams) {
        return patientSearchParams.toSearchParameterMap().getParameters().stream().map(entry -> {
            List<PropParam<?>> params = entry.getValue();
            switch (entry.getKey()) {
                case FhirConstants.NAME_SEARCH_HANDLER:
                    return processParamsEntry(params, param -> handleStringParam(param));
                case FhirConstants.IDENTIFIER_SEARCH_HANDLER:
                    return processParamsEntry(params, param -> handleIdentifierParam(param));
                case FhirConstants.GENDER_SEARCH_HANDLER:
                    return processParamsEntry(params, param -> handleTokenParam(param, null));
                case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
                    return processParamsEntry(params, param -> handleDateParam(param));
                case FhirConstants.BOOLEAN_SEARCH_HANDLER:
                    return processParamsEntry(params, param -> handleTokenParam(param, "deceased"));
                case FhirConstants.ADDRESS_SEARCH_HANDLER:
                    return processParamsEntry(params, param -> handleStringParam(param));
                case FhirConstants.COMMON_SEARCH_HANDLER:
                    return processParamsEntry(params, param -> handleCommonProps(param));
                default:
                    return Arrays.asList(Optional.<ICriterion<?>> empty());
            }
        }).flatMap(Collection::stream).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }
	
	private List<Optional<ICriterion<?>>> processParamsEntry(List<PropParam<?>> params,
            Function<PropParam<?>, List<Optional<ICriterion<?>>>> handler) {
        return params.stream().map(param -> handler.apply(param)).flatMap(Collection::stream).collect(Collectors.toList());
    }
	
	/**
	 * A generic handler for processing string parameters.
	 * 
	 * @param param the PropParam<?> containing the string parameter to handle
	 * @return a list of criterions representing the criteria created from the string parameter
	 */
	private List<Optional<ICriterion<?>>> handleStringParam(PropParam<?> param) {
        if (param == null || param.getParam() == null) {
            return Arrays.asList(Optional.empty());
        }
        String paramName = getFhirParamName(param);
        
        return extractStringParams((StringAndListParam) param.getParam()).stream()
                .map(stringParam -> createCriterionFromStringParam(paramName, stringParam)).collect(Collectors.toList());
    }
	
	/**
	 * A generic handler for processing token-based parameters.
	 * 
	 * @param param the PropParam<?> containing the token parameter to handle
	 * @param paramName optional; if provided, will be used as the param name
	 * @return a list of criterions representing the criteria created from the token parameter
	 */
	private List<Optional<ICriterion<?>>> handleTokenParam(PropParam<?> param, String paramName) {
        if (param == null || param.getParam() == null) {
            return Arrays.asList(Optional.empty());
        }
        List<TokenParam> tokenParams = extractTokenParams((TokenAndListParam) param.getParam());
        return tokenParams.stream()
                .map(token -> createCriterionFromTokenParam(
                    StringUtils.isNotBlank(paramName) ? paramName : getFhirParamName(param), token))
                .collect(Collectors.toList());
    }
	
	private List<Optional<ICriterion<?>>> handleIdentifierParam(PropParam<?> param) {
        if (param == null || param.getParam() == null) {
            return Arrays.asList(Optional.empty());
        }
        Map<String, List<TokenParam>> identifierTokens = extractTokenParamsBySystem((TokenAndListParam) param.getParam());
        List<Optional<ICriterion<?>>> criterions = identifierTokens.entrySet().stream()
                .map(e -> createIdentifierCriterion(e.getKey(), e.getValue())).collect(Collectors.toList());
        
        return criterions;
    }
	
	private List<Optional<ICriterion<?>>> handleDateParam(PropParam<?> param) {
        DateRangeParam dateRangeParam = (DateRangeParam) param.getParam();
        DateParam lower = dateRangeParam.getLowerBound();
        DateParam upper = dateRangeParam.getUpperBound();
        
        return Stream.of(hasDistinctRanges(lower, upper) ? new DateParam[] { lower, upper } : new DateParam[] { lower })
                .filter(Objects::nonNull).map(dateParam -> {
                    String prefix = dateParam.getPrefix() != null ? dateParam.getPrefix().getValue() : "eq";
                    String dateString = dateParam.getValueAsString();
                    return createCriterionFromTokenParam(getFhirParamName(param), new TokenParam(null, prefix + dateString));
                }).collect(Collectors.toList());
    }
	
	private List<Optional<ICriterion<?>>> handleCommonProps(PropParam<?> param) {
		switch (param.getPropertyName()) {
			case FhirConstants.LAST_UPDATED_PROPERTY:
				return handleDateParam(param);
			case FhirConstants.ID_PROPERTY:
				return handleTokenParam(param, null);
		}
		return Arrays.asList(Optional.empty());
	}
	
	private Optional<ICriterion<?>> createCriterionFromTokenParam(String paramName, TokenParam token) {
		String paramNameWithModifier = token.getModifier() != null ? paramName + token.getModifier().getValue() : paramName;
		return Optional.of(new TokenClientParam(paramNameWithModifier).exactly().code(token.getValue()));
	}
	
	private Optional<ICriterion<?>> createCriterionFromStringParam(String paramName, StringParam param) {
		String value = param.getValue();
		StringClientParam clientParam = new StringClientParam(paramName);
		if (param.isExact()) {
			return Optional.of(clientParam.matchesExactly().value(value));
		}
		return Optional.of(clientParam.matches().value(value));
	}
	
	private Optional<ICriterion<?>> createIdentifierCriterion(String system, List<TokenParam> identifierTokens) {
        if (identifierTokens == null || identifierTokens.isEmpty()) {
            return Optional.empty();
        }
        List<String> identifiers = identifierTokens.stream().map(TokenParam::getValue).collect(Collectors.toList());
        
        ICriterion<?> criterion;
        if (StringUtils.isNotBlank(system)) {
            criterion = Patient.IDENTIFIER.exactly().systemAndValues(system, identifiers);
        } else {
            criterion = Patient.IDENTIFIER.exactly().codes(identifiers);
        }
        return Optional.ofNullable(criterion);
    }
	
	private boolean hasDistinctRanges(DateParam lower, DateParam upper) {
		String lowerModifier = lower != null && lower.getPrefix() != null ? lower.getPrefix().getValue() : "";
		String upperModifier = upper != null && upper.getPrefix() != null ? upper.getPrefix().getValue() : "";
		return !lowerModifier.equals(upperModifier);
	}
	
	private List<StringParam> extractStringParams(StringAndListParam listParam) {
        return listParam.getValuesAsQueryTokens().stream().flatMap(token -> token.getValuesAsQueryTokens().stream())
                .collect(Collectors.toList());
    }
	
	private Map<String, List<TokenParam>> extractTokenParamsBySystem(TokenAndListParam listParam) {
        return extractTokenParams(listParam).stream()
                .collect(Collectors.groupingBy(token -> StringUtils.trimToEmpty(token.getSystem())));
    }
	
	private List<TokenParam> extractTokenParams(TokenAndListParam listParam) {
        return listParam.getValuesAsQueryTokens().stream().flatMap(token -> token.getValuesAsQueryTokens().stream())
                .collect(Collectors.toList());
    }
	
	private static String getFhirParamName(PropParam<?> param) {
		String fhirParamName = customPropertyToFhirMap.get(param.getPropertyName());
		return fhirParamName != null ? fhirParamName : param.getPropertyName();
	}
}
