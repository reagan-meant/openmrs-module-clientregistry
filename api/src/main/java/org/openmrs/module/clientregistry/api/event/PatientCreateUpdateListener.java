package org.openmrs.module.clientregistry.api.event;

import java.util.List;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.event.EventListener;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.clientregistry.ClientRegistryConfig;
import org.openmrs.module.clientregistry.ClientRegistryConstants;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.hl7.fhir.r4.model.Reference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PatientCreateUpdateListener implements EventListener {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private DaemonToken daemonToken;
	
	@Autowired
	private FhirPatientService patientService;
	
	@Autowired
	private ClientRegistryConfig config;
	
	@Autowired
	@Qualifier("clientRegistryFhirClient")
	private IGenericClient client;
	
	public DaemonToken getDaemonToken() {
		return daemonToken;
	}
	
	public void setDaemonToken(DaemonToken daemonToken) {
		this.daemonToken = daemonToken;
	}
	
	@Override
	public void onMessage(Message message) {
		log.trace(String.format("Received message: \n%s", message));

		try {
			Daemon.runInDaemonThread(() -> {
				try {
					processMessage(message);
				}
				catch (Exception e) {
					log.error(String.format("Failed to process Patient message!\n%s", message.toString()), e);
				}
			}, daemonToken);
		} catch (Exception e) {
			log.error(String.format("Failed to start Daemon thread to process message!\n%s", message.toString()), e);
		}

	}
	
	private void processMessage(Message message) throws JMSException {
		if (message instanceof MapMessage) {
			MapMessage mapMessage = (MapMessage) message;
			
			String uuid;
			try {
				uuid = mapMessage.getString("uuid");
				log.debug(String.format("Handling patient %s", uuid));
			}
			catch (JMSException e) {
				log.error("Exception caught while trying to get patient uuid for event.", e);
				return;
			}
			
			if (uuid == null || StringUtils.isBlank(uuid)) {
				return;
			}
			
			Patient patient;
			patient = patientService.get(uuid);
			patient.getNameFirstRep().setUse(HumanName.NameUse.OFFICIAL);
			
			Identifier openmrsUniqueId = new Identifier()
			        .setSystem(ClientRegistryConstants.CLIENT_REGISTRY_INTERNAL_ID_SYSTEM)
			        .setValue(String.format("%s/%s", config.getClientRegistryIdentifierRoot(), uuid))
			        .setUse(Identifier.IdentifierUse.OFFICIAL);
			patient.addIdentifier(openmrsUniqueId);
			
			patient.setId(openmrsUniqueId.getValue());
			
			if (mapMessage.getJMSDestination().toString().equals(ClientRegistryConstants.UPDATE_MESSAGE_DESTINATION)) {
				client.update().resource(patient).execute();
			} else {
				try {
					client.create().resource(patient).execute();
				}
				catch (Exception e) {
					List<org.hl7.fhir.r4.model.Patient> mypatientsByID = client
					.search()
					.forResource(org.hl7.fhir.r4.model.Patient.class)
					.where(org.hl7.fhir.r4.model.Patient.IDENTIFIER.exactly()
							.systemAndIdentifier(ClientRegistryConstants.CLIENT_REGISTRY_INTERNAL_ID_SYSTEM, String.format("%s/%s", config.getClientRegistryIdentifierRoot(), uuid)))
					.returnBundle(Bundle.class)
					.execute()
					.getEntry()
					.stream()
					.map(p -> (org.hl7.fhir.r4.model.Patient) p.getResource())
					.collect(Collectors.toList());

					List<Reference> links = mypatientsByID.get(0).getLink()
        .stream()
        .map(patientLink -> patientLink.getOther())
        .collect(Collectors.toList());
		String CRUID ="";
		for (Reference link : links) {
			CRUID = extractUUID(link.getReference());
		}
		org.openmrs.Patient p = Context.getPatientService().getPatientByUuid(uuid);
		PatientIdentifier pi = new PatientIdentifier();
		pi.setIdentifier(CRUID);
		pi.setIdentifierType(Context.getPatientService().getPatientIdentifierTypeByUuid("43a6e699-c2b8-4d5f-9e7f-cf19448d59b7"));
		pi.setLocation(Context.getLocationService().getDefaultLocation());
		pi.setPreferred(false);

		p.addIdentifier(pi);
		Context.getPatientService().savePatient(p);
			
					log.error("Exception caught while trying to get patient uuid for event.", e);
					return;
				}
				
			}
		}
	}
	
	public static String extractUUID(String url) {
		// Regular expression pattern for UUID
		Pattern pattern = Pattern.compile(".*/([a-fA-F0-9\\-]+)$");
		Matcher matcher = pattern.matcher(url);
		
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			// UUID not found
			return null;
		}
	}
	
}
