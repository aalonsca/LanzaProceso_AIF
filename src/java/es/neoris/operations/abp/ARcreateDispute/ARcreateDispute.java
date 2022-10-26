package es.neoris.operations.abp.ARcreateDispute;

import java.util.HashMap;
import java.util.Properties;

import amdocs.ar.datalayer.datatypes.AccountIdInfoDt;
import amdocs.ar.datalayer.datatypes.DisputeDetailsDt;
import amdocs.ar.datalayer.datatypes.DisputePDt;
import amdocs.ar.sessions.interfaces.api.ARBIRDisputeServices;
import es.neoris.operations.BaseAIF;

public class ARcreateDispute 
extends BaseAIF {

	// Properties from .properties file
	static final String sDirEject = "es/neoris/operations/abp/ARcreateDispute/";
	static final String sNombreFich = "ARcreateDispute.properties";
	static final String sRutaIni = "res/";
	
	// Properties for WL connection
	protected static Boolean debugMode = false;
	private HashMap<String, String> connectionProp = new HashMap<String, String>();
	private static final String JNDI = "/amdocsBeans/ARBIRDisputeServices";
	private static ARBIRDisputeServices service = null;
	
	// Variables to call service
	private AccountIdInfoDt i_accountIDInfo;
	private DisputeDetailsDt i_disputeDetailsDT;
	private DisputePDt o_result;

	// Variables sent by the service
	private String cmAccountNumber;
	private long accountId;
	private double amount;
	

	/**
	 *  Default no-operative constructor
	 */
	public ARcreateDispute () {
		// no-op constructor
	}
	
	
	/**
	 * Reads .properties file to prepare a connection to WL
	 * @param getConf
	 * @throws Exception
	 */
	public ARcreateDispute(Boolean getConf) 
		throws Exception { 
		
		if (getConf) {
			//Get info from .properties files 
			try {
				Properties properties = BaseAIF.getProperties(sRutaIni, sNombreFich);
				
				connectionProp.put("WLS_URL", properties.getProperty("WLS_URL"));
				connectionProp.put("WLS_USER", properties.getProperty("WLS_USER"));
				connectionProp.put("WLS_PASS", properties.getProperty("WLS_PASS"));
				connectionProp.put("DEBUG", properties.getProperty("DEBUG"));

				if ((!"".equals(properties.getProperty("DEBUG"))) && "1".equals(properties.getProperty("DEBUG"))) 
					ARcreateDispute.debugMode = true;				
				
			}
			catch (Exception e) {
				
				System.out.println("Error getting " + sNombreFich + ": " + e.getLocalizedMessage());
				throw new Exception("Error reading .properties file");
			}
		}
		
	}
	

	/**
	 * Prepared WL connection to execute the operation
	 * @return 0 -> OK
	 *        -1 -> Error connecting
	 */
	public DisputePDt execProc() {

		i_accountIDInfo = new AccountIdInfoDt();
		i_disputeDetailsDT = new DisputeDetailsDt();
		o_result = new DisputePDt();


		if (ARcreateDispute.debugMode) {
			System.out.println("Entering execProcess");			
		}
		

		try {

			//Open WL connection through RMI
			service =((ARBIRDisputeServices) BaseAIF.createEJBObject(connectionProp, JNDI, ARcreateDispute.debugMode));
			
			// Fill the input parameters
			setAccountInfo();
			setDisputeDetails();
			
			//Call the AIF service
			o_result = service.createDispute(i_accountIDInfo, i_disputeDetailsDT);
			
			if (ARcreateDispute.debugMode) {
				System.out.println("Object created.");
			}
		
				
		}catch(Exception e) {
		
			if (ARcreateDispute.debugMode) {
				System.out.println("ERROR getting EPISession. Exiting..." + e.toString());				
			}
			
			return o_result;
		}
		
		
		return o_result;
		
	}
	
	
	private void setAccountInfo() {
		
		i_accountIDInfo.setAccountId(this.accountId);
		i_accountIDInfo.setCmAccountNumber(this.cmAccountNumber);		
		
	}
	
	private void setDisputeDetails() {
		i_disputeDetailsDT.setAmount(this.amount);
		
	}
	
	
	public AccountIdInfoDt getI_accountIDInfo() {
		return i_accountIDInfo;
	}


	public void setI_accountIDInfo(AccountIdInfoDt i_accountIDInfo) {
		this.i_accountIDInfo = i_accountIDInfo;
	}


	public DisputeDetailsDt getI_disputeDetailsDT() {
		return i_disputeDetailsDT;
	}


	public void setI_disputeDetailsDT(DisputeDetailsDt i_disputeDetailsDT) {
		this.i_disputeDetailsDT = i_disputeDetailsDT;
	}


	public DisputePDt getO_result() {
		return o_result;
	}


	public void setO_result(DisputePDt o_result) {
		this.o_result = o_result;
	}

	public String getCmAccountNumber() {
		return cmAccountNumber;
	}


	public void setCmAccountNumber(String cmAccountNumber) {
		this.cmAccountNumber = cmAccountNumber;
	}


	public long getAccountId() {
		return accountId;
	}


	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}


	public double getAmount() {
		return amount;
	}


	public void setAmount(double amount) {
		this.amount = amount;
	}
	
}
