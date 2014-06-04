package org.ihtsdo.sct.drugmatch.id.service.impl;

import java.math.BigInteger;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.ihtsdo.sct.drugmatch.constant.rf2.ReleaseFormat2;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.id.service.IdService;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.id.service.CreateConceptIdsFaultException;
import org.ihtsdo.sct.id.service.CreateSCTIDFaultException;
import org.ihtsdo.sct.id.service.Id_generatorStub;
import org.ihtsdo.sct.id.service.Id_generatorStub.CreateConceptIdsRequest;
import org.ihtsdo.sct.id.service.Id_generatorStub.CreateConceptIdsResponse;
import org.ihtsdo.sct.id.service.Id_generatorStub.CreateSCTIDRequest;
import org.ihtsdo.sct.id.service.Id_generatorStub.CreateSCTIDResponse;
import org.ihtsdo.sct.id.service.Id_generatorStub.IDString;

/**
 * @author dev-team@carecom.dk
 */
public class IdServiceImpl implements IdService {

	private final Id_generatorStub service = new Id_generatorStub(DrugMatchProperties.getSctIdService());

	/**
	 * Namespace constant.
	 */
	private final BigInteger namespaceExtensionId = new BigInteger(DrugMatchProperties.getNationalNamespaceId());

	/**
	 * @throws AxisFault
	 * @throws DrugMatchConfigurationException
	 */
	public IdServiceImpl() throws AxisFault, DrugMatchConfigurationException {
		// empty on purpose
	}

	/**
	 * Create concept ID request.
	 * @param uuid
	 * @param parentId
	 * @param namespaceId
	 * @param partitionId
	 * @return
	 * @throws DrugMatchConfigurationException
	 */
	private static CreateConceptIdsRequest getCreateConceptIdsRequest(final String uuid,
			final String parentId,
			final BigInteger namespaceId,
			final String partitionId) throws DrugMatchConfigurationException {
		CreateConceptIdsRequest request = new CreateConceptIdsRequest();
		request.setComponentUuid(uuid);
		request.setExecutionId(DrugMatchProperties.getSctReleaseId());
		request.setModuleId(DrugMatchProperties.getModuleId());
		request.setNamespaceId(namespaceId);
		request.setParentSnomedId(parentId);
		request.setPartitionId(partitionId);
		request.setReleaseId(DrugMatchProperties.getSctReleaseId());
		return request;
	}

	/**
	 * Create SCT ID request.
	 * @param componentUuid
	 * @param namespaceId
	 * @param partitionId
	 * @return
	 * @throws DrugMatchConfigurationException
	 */
	private static CreateSCTIDRequest getCreateSCTIDRequest(final String componentUuid,
			final BigInteger namespaceId,
			final String partitionId) throws DrugMatchConfigurationException {
		CreateSCTIDRequest request = new CreateSCTIDRequest();
		request.setComponentUuid(componentUuid);
		request.setExecutionId(DrugMatchProperties.getSctReleaseId());
		request.setModuleId(DrugMatchProperties.getModuleId());
		request.setNamespaceId(namespaceId);
		request.setPartitionId(partitionId);
		request.setReleaseId(DrugMatchProperties.getSctReleaseId());
		return request;
	}

	/**
	 * @param request
	 * @return SCT component ID
	 * @throws CreateSCTIDFaultException
	 * @throws RemoteException
	 */
	private String getComponentId(final CreateSCTIDRequest request) throws CreateSCTIDFaultException, RemoteException {
		CreateSCTIDResponse response = this.service.createSCTID(request);
		return (response.getSctId() == null) ? null : response.getSctId().toString();
	}

	/**
	 * @param request
	 * @return SCT concept ID
	 * @throws CreateConceptIdsFaultException
	 * @throws RemoteException
	 */
	private String getConceptId(final CreateConceptIdsRequest request) throws CreateConceptIdsFaultException, RemoteException {
		CreateConceptIdsResponse response = this.service.createConceptIds(request);
		IDString componentId = null;
		for (IDString idString : response.getConceptIds()) {
			if (idString.getIdentifier().intValue() == 2) {
				componentId = idString;
				break;
			}
		}
		return (componentId == null) ? null : componentId.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getExtensionConceptId(final String uuid,
			final String parentId) throws CreateConceptIdsFaultException, DrugMatchConfigurationException, RemoteException {
		CreateConceptIdsRequest request = getCreateConceptIdsRequest(uuid,
				parentId,
				this.namespaceExtensionId,
				ReleaseFormat2.PARTITION_EXTENSION_CONCEPT_ID);
		return getConceptId(request);
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getExtensionDescriptionId(final String uuid) throws CreateSCTIDFaultException, DrugMatchConfigurationException, RemoteException {
		CreateSCTIDRequest request = getCreateSCTIDRequest(uuid,
				this.namespaceExtensionId,
				ReleaseFormat2.PARTITION_EXTENSION_DESCRIPTION_ID);
		return getComponentId(request);
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getExtensionRelationshipId(final String uuid) throws CreateSCTIDFaultException, DrugMatchConfigurationException, RemoteException {
		CreateSCTIDRequest request = getCreateSCTIDRequest(uuid,
				this.namespaceExtensionId,
				ReleaseFormat2.PARTITION_EXTENSION_RELATIONSHIP_ID);
		return getComponentId(request);
	}
}
