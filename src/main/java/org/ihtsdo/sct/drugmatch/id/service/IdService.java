package org.ihtsdo.sct.drugmatch.id.service;

import java.rmi.RemoteException;

import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.id.service.CreateConceptIdsFaultException;
import org.ihtsdo.sct.id.service.CreateSCTIDFaultException;

/**
 * @author dev-team@carecom.dk
 */
public interface IdService {

	/**
	 * Get SNOMED CT extension concept ID.
	 * @param uuid
	 * @param parentId
	 * @return
	 * @throws CreateConceptIdsFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws RemoteException
	 */
	String getExtensionConceptId(String uuid,
			String parentId) throws CreateConceptIdsFaultException, DrugMatchConfigurationException, RemoteException;

	/**
	 * Get SNOMED CT extension description ID.
	 * @param uuid
	 * @return
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws RemoteException
	 */
	String getExtensionDescriptionId(String uuid) throws CreateSCTIDFaultException, DrugMatchConfigurationException, RemoteException;

	/**
	 * Get SNOMED CT extension relationship ID.
	 * @param uuid
	 * @return
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws RemoteException
	 */
	String getExtensionRelationshipId(String uuid) throws CreateSCTIDFaultException, DrugMatchConfigurationException, RemoteException;
}
