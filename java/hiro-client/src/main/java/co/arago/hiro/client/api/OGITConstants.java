package co.arago.hiro.client.api;

/**
 * GraphIT-related ontology constants.
 */
public interface OGITConstants {

  String NAMESPACE_SEPERATOR = "/";
  String NAMESPACE_OGIT = "ogit" + NAMESPACE_SEPERATOR;
  String NAMESPACE_AUTOMATION = NAMESPACE_OGIT + "Automation" + NAMESPACE_SEPERATOR;
  String NAMESPACE_MARS = NAMESPACE_OGIT + "MARS" + NAMESPACE_SEPERATOR;
  String NAMESPACE_NETWORK = NAMESPACE_OGIT + "Network" + NAMESPACE_SEPERATOR;
  String NAMESPACE_SERVICEMANAGEMENT = NAMESPACE_OGIT + "ServiceManagement" + NAMESPACE_SEPERATOR;
  String NAMESPACE_DATA = NAMESPACE_OGIT + "Data" + NAMESPACE_SEPERATOR;
  String NAMESPACE_DATAPROCESSING = NAMESPACE_OGIT + "DataProcessing" + NAMESPACE_SEPERATOR;
  String NAMESPACE_SOFTWARE = NAMESPACE_OGIT + "Software" + NAMESPACE_SEPERATOR;
  String NAMESPACE_AUTH = NAMESPACE_OGIT + "Auth" + NAMESPACE_SEPERATOR;
  String NAMESPACE_AUTH_APPLICATION = NAMESPACE_AUTH + "Application" + NAMESPACE_SEPERATOR;

  String NAMESPACE_PURL = "http://www.purl.org/";

  String PROP_FROM = "_from";
  String PROP_TO = "_to";
  String GRAPH_TYPE = "ontology";

  interface Attributes {

    //core
    String OGIT__CREATED_ON = NAMESPACE_OGIT + "_created-on";
    String OGIT__CONTENT = NAMESPACE_OGIT + "_content";
    String OGIT__CREATOR = NAMESPACE_OGIT + "_creator";
    String OGIT__CREATOR_APP = NAMESPACE_OGIT + "_creator-app";
    String OGIT__DELETED_ON = NAMESPACE_OGIT + "_deleted-on";
    String OGIT__DELETED_BY = NAMESPACE_OGIT + "_deleted-by";
    String OGIT__DELETED_BY_APP = NAMESPACE_OGIT + "_deleted-by-app";
    String OGIT__EDGE_ID = NAMESPACE_OGIT + "_edge-id";
    String OGIT__GRAPHTYPE = NAMESPACE_OGIT + "_graphtype";
    String OGIT__GRAPHVERSION = NAMESPACE_OGIT + "_v";
    String OGIT__GRAPHVERSION_ID = NAMESPACE_OGIT + "_v-id";
    String OGIT__IN_VERSION_ID = NAMESPACE_OGIT + "_in-v-id";
    String OGIT__OUT_VERSION_ID = NAMESPACE_OGIT + "_out-v-id";
    String OGIT__ID = NAMESPACE_OGIT + "_id";
    String OGIT__XID = NAMESPACE_OGIT + "_xid";
    String OGIT__IN_ID = NAMESPACE_OGIT + "_in-id";
    String OGIT__IS_DELETED = NAMESPACE_OGIT + "_is-deleted";
    String OGIT__MODIFIED_BY = NAMESPACE_OGIT + "_modified-by";
    String OGIT__MODIFIED_BY_APP = NAMESPACE_OGIT + "_modified-by-app";
    String OGIT__MODIFIED_ON = NAMESPACE_OGIT + "_modified-on";
    String OGIT__OUT_ID = NAMESPACE_OGIT + "_out-id";
    String OGIT__OUT_TYPE = NAMESPACE_OGIT + "_out-type";
    String OGIT__IN_TYPE = NAMESPACE_OGIT + "_in-type";
    String OGIT__OWNER = NAMESPACE_OGIT + "_owner";
    String OGIT__READER = NAMESPACE_OGIT + "_reader";
    String OGIT__TAGS = NAMESPACE_OGIT + "_tags";
    String OGIT__TYPE = NAMESPACE_OGIT + "_type";
    String OGIT__SOURCE = NAMESPACE_OGIT + "_source";
    String OGIT__SECURITY_TYPES = NAMESPACE_OGIT + "_security-types";

    //SGO
    String OGIT_ADMIN_CONTRACT = NAMESPACE_OGIT + "admin-contact";
    String OGIT_ASSIGNEDGROUP = NAMESPACE_OGIT + "assignedGroup";
    String OGIT_ACCESSCONTROL = NAMESPACE_OGIT + "accessControl";
    String OGIT_CITYPE = NAMESPACE_OGIT + "ciType";
    String OGIT_CONTENT = NAMESPACE_OGIT + "content";
    String OGIT_CONNECTOR_ID = NAMESPACE_OGIT + "connectorId";
    String OGIT_CREATOR = NAMESPACE_OGIT + "creator";
    String OGIT_CREATIONTIME = NAMESPACE_OGIT + "creationTime";
    String OGIT_DATE = NAMESPACE_OGIT + "date";
    String OGIT_DESCRIPTION = NAMESPACE_OGIT + "description";
    String OGIT_EMAIL = NAMESPACE_OGIT + "email";
    String OGIT_EXPIRATIONDATE = NAMESPACE_OGIT + "expirationDate";
    String OGIT_FUNCTION = NAMESPACE_OGIT + "function";
    String OGIT_HISTORY = NAMESPACE_OGIT + "history";
    String OGIT_INFORMATION = NAMESPACE_OGIT + "information";
    String OGIT_ID = NAMESPACE_OGIT + "id";
    String OGIT_ISROOT = NAMESPACE_OGIT + "isRoot";
    String OGIT_ISVALID = NAMESPACE_OGIT + "isValid";
    String OGIT_LICENSEKEY = NAMESPACE_OGIT + "licenseKey";
    String OGIT_LICENSEREQUESTSTATUS = NAMESPACE_OGIT + "licenseRequestStatus";
    String OGIT_LICENSETYPE = NAMESPACE_OGIT + "licenseType";
    String OGIT_LICENSEID = NAMESPACE_OGIT + "licenseId";
    String OGIT_MESSAGE = NAMESPACE_OGIT + "message";
    String OGIT_MODIFICATIONTIME = NAMESPACE_OGIT + "modificationTime";
    String OGIT_MODIFIED = NAMESPACE_OGIT + "modified";
    String OGIT_NAME = NAMESPACE_OGIT + "name";
    String OGIT_ONTOLOGY_ADMINCONTACT = NAMESPACE_OGIT + "ontologyAdminContact";
    String OGIT_ONTOLOGY_CARDINALITY = NAMESPACE_OGIT + "ontologyCardinality";
    String OGIT_ONTOLOGY_CREATOR = NAMESPACE_OGIT + "ontologyCreator";
    String OGIT_ONTOLOGY_DELETER = NAMESPACE_OGIT + "ontologyDeleter";
    String OGIT_ONTOLOGY_MODIFIED = NAMESPACE_OGIT + "ontologyModified";
    String OGIT_ONTOLOGY_NAME = NAMESPACE_OGIT + "ontologyName";
    String OGIT_ONTOLOGY_SCOPE = NAMESPACE_OGIT + "ontologyScope";
    String OGIT_ONTOLOGY_TECHCONTACT = NAMESPACE_OGIT + "ontologyTechContact";
    String OGIT_ONTOLOGY_TYPE = NAMESPACE_OGIT + "ontologyType";
    String OGIT_ONTOLOGY_VALIDFROM = NAMESPACE_OGIT + "ontologyValidFrom";
    String OGIT_ONTOLOGY_VALIDUNTIL = NAMESPACE_OGIT + "ontologyValidUntil";
    String OGIT_ONTOLOGY_VALIDATIONPARAMETER = NAMESPACE_OGIT + "ontologyValidationParameter";
    String OGIT_ONTOLOGY_VALIDATIONTYPE = NAMESPACE_OGIT + "ontologyValidationType";
    String OGIT_REPORTDATA = NAMESPACE_OGIT + "reportData";
    String OGIT_REPORTTYPE = NAMESPACE_OGIT + "reportType";
    String OGIT_RESPONSE = NAMESPACE_OGIT + "response";
    String OGIT_SCOPE = NAMESPACE_OGIT + "scope";
    String OGIT_SIZE = NAMESPACE_OGIT + "size";
    String OGIT_STATUS = NAMESPACE_OGIT + "status";
    String OGIT_SUBJECT = NAMESPACE_OGIT + "subject";
    String OGIT_SUBTYPE = NAMESPACE_OGIT + "subType";
    String OGIT_TECH_CONTACT = "tech-contact";
    String OGIT_TIMESTAMP = NAMESPACE_OGIT + "timestamp";
    String OGIT_TOKEN = NAMESPACE_OGIT + "token";
    String OGIT_PARENT = NAMESPACE_OGIT + "parent";
    String OGIT_QUESTION = NAMESPACE_OGIT + "question";
    String OGIT_URI = NAMESPACE_OGIT + "uri";
    String OGIT_URL = NAMESPACE_OGIT + "url";
    String OGIT_VALIDFROM = NAMESPACE_OGIT + "validFrom";
    String OGIT_VALIDUNTIL = NAMESPACE_OGIT + "validUntil";
    String OGIT_VALIDTO = NAMESPACE_OGIT + "validTo";
    String OGIT_VERSION = NAMESPACE_OGIT + "_version";
    String OGITVERSION = NAMESPACE_OGIT + "version";
    String OGIT_YAML_ONTOLOGY_ID = NAMESPACE_OGIT + "OGIT";
    String OGIT_FIRSTNAME = NAMESPACE_OGIT + "firstName";
    String OGIT_LASTNAME = NAMESPACE_OGIT + "lastName";
    String OGIT_ALTERNATIVENAME = NAMESPACE_OGIT + "alternativeName";
    String OGIT_TITLE = NAMESPACE_OGIT + "title";
    String OGIT_TYPE = NAMESPACE_OGIT + "type";
    String OGIT_ONVERTEX = NAMESPACE_OGIT + "onVertex";
    String OGIT_ONATTRIBUTE = NAMESPACE_OGIT + "onAttribute";
    String OGIT_OPERATION = NAMESPACE_OGIT + "operation";
    String OGIT_CONDITION = NAMESPACE_OGIT + "condition";
    String OGIT_LAST_OCCURRED_AT = NAMESPACE_OGIT + "lastOccurredAt";
    String OGIT_LAST_UPDATED_AT = NAMESPACE_OGIT + "lastUpdatedAt";
    String OGIT_SOURCE_ID = NAMESPACE_OGIT + "sourceId";
    String OGIT_LOCKED = NAMESPACE_OGIT + "locked";

    //AUTOMATION
    String AUTOMATION_AUTOMATION_STATE = NAMESPACE_AUTOMATION + "automationState";
    String AUTOMATION_AFFECTEDNODEID = NAMESPACE_AUTOMATION + "affectedNodeId";
    String AUTOMATION_COMMAND = NAMESPACE_AUTOMATION + "command";
    String AUTOMATION_COMPANY_NAME = NAMESPACE_AUTOMATION + "companyName";
    String AUTOMATION_DEPLOYTOENGINE = NAMESPACE_AUTOMATION + "deployToEngine";
    String AUTOMATION_IS_DEPLOYED = NAMESPACE_AUTOMATION + "isDeployed";
    String AUTOMATION_DEPLOY_STATUS = NAMESPACE_AUTOMATION + "deployStatus";
    String AUTOMATION_ISSUEFORMALREPRESENTATION = NAMESPACE_AUTOMATION + "issueFormalRepresentation";
    String AUTOMATION_KNOWLEDGEITEMID = NAMESPACE_AUTOMATION + "knowledgeItemId";
    String AUTOMATION_KNOWLEDGEITEMFORMALREPRESENTATION = NAMESPACE_AUTOMATION + "knowledgeItemFormalRepresentation";
    String AUTOMATION_KNOWLEDGEITEMTIER = NAMESPACE_AUTOMATION + "knowledgeItemTier";
    String AUTOMATION_KNOWLEDGEITEM_SYNTAXVERSION = NAMESPACE_AUTOMATION + "knowledgeItemSyntaxVersion";
    String AUTOMATION_LOGLEVEL = NAMESPACE_AUTOMATION + "logLevel";
    String AUTOMATION_MAIDFORMALREPRESENTAION = NAMESPACE_AUTOMATION + "maidFormalRepresentation";
    String AUTOMATION_MARSNODEFORMALREPRESENTATION = NAMESPACE_AUTOMATION + "marsNodeFormalRepresentation";
    String AUTOMATION_MARSNODETYPE = NAMESPACE_AUTOMATION + "marsNodeType";
    String AUTOMATION_PROJECT_NAME = NAMESPACE_AUTOMATION + "projectName";
    String AUTOMATION_SERVICETYPE = NAMESPACE_AUTOMATION + "serviceType";
    String AUTOMATION_SERVICESTATUS = NAMESPACE_AUTOMATION + "serviceStatus";
    String AUTOMATION_SYSTEMTYPE = NAMESPACE_AUTOMATION + "systemType";
    String AUTOMATION_PROCESSINGTIMESTAMP = NAMESPACE_AUTOMATION + "processingTimestamp";
    String AUTOMATION_IS_TODO = NAMESPACE_AUTOMATION + "todo";
    String AUTOMATION_LIFECYCLE = NAMESPACE_AUTOMATION + "lifecycle";
    String AUTOMATION_ISSUE_ORIGIN_NODE = NAMESPACE_AUTOMATION + "originNode";
    String AUTOMATION_ISSUE_PROCESSING_NODE = NAMESPACE_AUTOMATION + "processingNode";

    //MARS
    String MARS_NETWORK_FQDN = NAMESPACE_MARS + NAMESPACE_SEPERATOR
      + NAMESPACE_NETWORK + NAMESPACE_SEPERATOR + "fqdn";

    //SERVICEMANAGEMENT
    String SERVICEMANAGEMENT_LICENSETOKENSTATUS = NAMESPACE_SERVICEMANAGEMENT + "licenseTokenStatus";
    String SERVICEMANAGEMENT_INCIDENTSTATUS = NAMESPACE_SERVICEMANAGEMENT + "incidentStatus";
    String SERVICEMANAGEMENT_CHANGEREQUESTSTATUS = NAMESPACE_SERVICEMANAGEMENT + "changeStatus";
    String SERVICEMANAGEMENT_SUBTASKSTATUS = NAMESPACE_SERVICEMANAGEMENT + "taskStatus";

    //DATA
    String DATA_TIMETOLIVE = NAMESPACE_DATA + "timeToLive";

    //DATAPROCESSING
    String DATAPROCESSING_QUERYTYPE = NAMESPACE_DATAPROCESSING + "queryType";
    String DATAPROCESSING_QUERY = NAMESPACE_DATAPROCESSING + "query";
    String DATAPROCESSING_PARAMETERS = NAMESPACE_DATAPROCESSING + "parameters";
    String DATAPROCESSING_OUTPUTTYPE = NAMESPACE_DATAPROCESSING + "outputType";
    String DATAPROCESSING_STATE = NAMESPACE_DATAPROCESSING + "state";
    String DATAPROCESSING_INTERNALJOBID = NAMESPACE_DATAPROCESSING + "internalJobId";

    //AUTH
    String AUTH_IAM_DOMAIN = NAMESPACE_AUTH + "_iam-domain";
    //AUTH/APPLICATION
    String AUTH_APPLICATION_TYPE = NAMESPACE_AUTH_APPLICATION + "type";
    String AUTH_APPLICATION_STATUS = NAMESPACE_AUTH_APPLICATION + "status";
    String AUTH_APPLICATION_PARENT = NAMESPACE_AUTH_APPLICATION + "parent";
    String AUTH_APPLICATION_URLS = NAMESPACE_AUTH_APPLICATION + "urls";
    String AUTH_APPLICATION_ALLOWEDTYPES = NAMESPACE_AUTH_APPLICATION + "allowedTypes";

    //free attributes
    String FREE_ATTRIBUTE_IDENTITY = "/identity";
    String FREE_ATTRIBUTE_PROFILESET = "/profileSet";
    String FREE_ATTRIBUTE_AUTOPILOT_VERSION = "/autopilotVersion";
  }

  interface Entities {

    //SGO
    String OGIT_ATTACHMENT = NAMESPACE_OGIT + "Attachment";
    String OGIT_CUSTOM_ENTITY = NAMESPACE_OGIT + "Custom/Entity";
    String OGIT_EVENT = NAMESPACE_OGIT + "Event";
    String OGIT_LICENSE = NAMESPACE_OGIT + "License";
    String OGIT_LICENSEREQUEST = NAMESPACE_OGIT + "LicenseRequest";
    String OGIT_LICENSETOKEN = NAMESPACE_OGIT + "LicenseToken";
    String OGIT_NODE = NAMESPACE_OGIT + "Node";
    String OGIT_ONTOLOGYATTRIBUTE = NAMESPACE_OGIT + "OntologyAttribute";
    String OGIT_ONTOLOGYENTITY = NAMESPACE_OGIT + "OntologyEntity";
    String OGIT_ONTOLOGYVERB = NAMESPACE_OGIT + "OntologyVerb";
    String OGIT_ORGANIZATION = NAMESPACE_OGIT + "Organization";
    String OGIT_PERSON = NAMESPACE_OGIT + "Person";
    String OGIT_POLICY = NAMESPACE_OGIT + "Policy";
    String OGIT_QUESTION = NAMESPACE_OGIT + "Question";
    String OGIT_SCHEMA = NAMESPACE_OGIT + "Schema";
    String OGIT_SOFTWARE_APPLICATION = NAMESPACE_OGIT + "Software/Application";
    String OGIT_SOFTWARE_CONNECTOR = NAMESPACE_OGIT + "Software/Connector";
    String OGIT_TASK = NAMESPACE_OGIT + "Task";
    String OGIT_TIMESERIES = NAMESPACE_OGIT + "Timeseries";
    String OGIT_ACCOUNT = NAMESPACE_OGIT + "Account";
    String OGIT_CONTRACT = NAMESPACE_OGIT + "Contract";
    String OGIT_COMMENT = NAMESPACE_OGIT + "Comment";
    String OGIT_EMAIL = NAMESPACE_OGIT + "Email";
    String OGIT_LOCATION = NAMESPACE_OGIT + "Location";
    String OGIT_CERTIFICATE = NAMESPACE_OGIT + "Certificate";
    String OGIT_NOTIFICATION = NAMESPACE_OGIT + "Notification";
    String OGIT_SUBSCRIPTION = NAMESPACE_OGIT + "Subscription";

    //AUTOMATION
    String AUTOMATION_AUTOMATIONISSUE = NAMESPACE_AUTOMATION + "AutomationIssue";
    String AUTOMATION_DYNAMICENGINEDATA = NAMESPACE_AUTOMATION + "DynamicEngineData";
    String AUTOMATION_HISTORY = NAMESPACE_AUTOMATION + "History";
    String AUTOMATION_KNOWLEDGEITEM = NAMESPACE_AUTOMATION + "KnowledgeItem";
    String AUTOMATION_MAID = NAMESPACE_AUTOMATION + "MAID";
    String AUTOMATION_MARSNODE = NAMESPACE_AUTOMATION + "MARSNode";
    String AUTOMATION_MARSMODEL = NAMESPACE_AUTOMATION + "MARSModel";
    String AUTOMATION_VARIABLE = NAMESPACE_AUTOMATION + "Variable";

    //MARS
    String MARS_APPLICATION = NAMESPACE_MARS + "Application";
    String MARS_MACHINE = NAMESPACE_MARS + "Machine";
    String MARS_RESOURCE = NAMESPACE_MARS + "Resource";
    String MARS_SOFTWARE = NAMESPACE_MARS + "Software";

    //SERVICEMANAGEMENT
    String SERVICEMANAGEMENT_REPORT = NAMESPACE_SERVICEMANAGEMENT + "Report";
    String SERVICEMANAGEMENT_INCIDENT = NAMESPACE_SERVICEMANAGEMENT + "Incident";
    String SERVICEMANAGEMENT_TICKET = NAMESPACE_SERVICEMANAGEMENT + "Ticket";
    String SERVICEMANAGEMENT_SUBTASK = NAMESPACE_SERVICEMANAGEMENT + "SubTask";
    String SERVICEMANAGEMENT_CHANGEREQUEST = NAMESPACE_SERVICEMANAGEMENT + "ChangeRequest";

    //DATA
    String DATA_LOG = NAMESPACE_DATA + "Log";

    //DATAPROCESSING
    String DATAPROCESSING_PROGRAM = NAMESPACE_DATAPROCESSING + "Program";

    //SOFTWARE
    String SOFTWARE_APPLICATION = NAMESPACE_SOFTWARE + "Application";

    //AUTH
    String AUTH_ACCOUNT = NAMESPACE_AUTH + "Account";
    String AUTH_ROLE = NAMESPACE_AUTH + "Role";
    String AUTH_APPLICATION = NAMESPACE_AUTH + "Application";
  }

  interface Verbs {

    //graph
    String OGIT__CONSENTS = NAMESPACE_OGIT + "_consents";

    //core
    String OGIT__CREATED = NAMESPACE_OGIT + "_created";
    String OGIT__OWNS = NAMESPACE_OGIT + "_owns";
    //SGO
    String OGIT_CUSTOM_VERB = NAMESPACE_OGIT + "Custom/Verb";
    String OGIT_AFFECTS = NAMESPACE_OGIT + "affects";
    String OGIT_BELONGS = NAMESPACE_OGIT + "belongs";
    String OGIT_CONTAINS = NAMESPACE_OGIT + "contains";
    String OGIT_CORRESPONDS = NAMESPACE_OGIT + "corresponds";
    String OGIT_CREATES = NAMESPACE_OGIT + "creates";
    String OGIT_DEMANDS = NAMESPACE_OGIT + "demands";
    String OGIT_DEPENDS_ON = NAMESPACE_OGIT + "dependsOn";
    String OGIT_DEPLOAYED_TO = NAMESPACE_OGIT + "deployedTo";
    String OGIT_GENERATES = NAMESPACE_OGIT + "generates";
    String OGIT_HAS_VERB = NAMESPACE_OGIT + "hasVerb";
    String OGIT_HOSTS = NAMESPACE_OGIT + "hosts";
    String OGIT_IS_CHILDOF = NAMESPACE_OGIT + "_spans";
    String OGIT_MANAGES = NAMESPACE_OGIT + "manages";
    String OGIT_OPTS = NAMESPACE_OGIT + "opts";
    String OGIT_INDEX = NAMESPACE_OGIT + "index";
    String OGIT_PROVIDES = NAMESPACE_OGIT + "provides";
    String OGIT_RELATES = NAMESPACE_OGIT + "relates";
    String OGIT_USES = NAMESPACE_OGIT + "uses";
    String OGIT_WORKSON = NAMESPACE_OGIT + "worksOn";
    String OGIT_DEFINES = NAMESPACE_OGIT + "defines";
    String OGIT_GOVERNS = NAMESPACE_OGIT + "governs";
    String OGIT_REPORTS = NAMESPACE_OGIT + "reports";
    String OGIT_RUNS_ON = NAMESPACE_OGIT + "runsOn";
    String OGIT_CONNECTS = NAMESPACE_OGIT + "connects";
    String OGIT_INVITES = NAMESPACE_OGIT + "invites";
    String OGIT_LIKES = NAMESPACE_OGIT + "likes";
    String OGIT_DISLIKES = NAMESPACE_OGIT + "dislikes";
    String OGIT_FOLLOWS = NAMESPACE_OGIT + "follows";
    String OGIT_SUPPORTS = NAMESPACE_OGIT + "supports";
    String OGIT_COMPETES = NAMESPACE_OGIT + "competes";
    String OGIT_CONCLUDES = NAMESPACE_OGIT + "concludes";
    String OGIT_SELLS_TO = NAMESPACE_OGIT + "sellsTo";
    String OGIT_DELIVERS_TO = NAMESPACE_OGIT + "deliversTo";
    String OGIT_COMPLIES = NAMESPACE_OGIT + "complies";
    String OGIT_ASSIGNED_TO = "ogit/assignedTo";
    String OGIT_TRIGGERS = NAMESPACE_OGIT + "triggers";
    String OGIT_ALERTS = NAMESPACE_OGIT + "alerts";
    String OGIT_SUBSCRIBES = NAMESPACE_OGIT + "subscribes";
    String OGIT_INCLUDES = NAMESPACE_OGIT + "includes";

    //AUTH
    String AUTH_ASSUMES = NAMESPACE_AUTH + "assumes";
  }

  /**
   * defines some free attributes for ogit/Timeseries that have common some
   * common use
   *
   */
  interface TimeseriesFreeAttributes {

    String TS_NODEID = "nodeID";
    String TS_DATA_TYPE = "DataType";
    String TS_INSTANCEID = "InstanceID";
    String TS_MAID_TYPE = "MAIDType";
    String TS_DATA_NAME = "DataName";
    String TS_MONITOR_NODEID = "MonitorNodeID";
    String TS_VALUE_STORED_FROM = "KeyValueStore.StoredFrom";
    String TS_VALUE_STORED_TO = "KeyValueStore.StoredTo";
  }
}
