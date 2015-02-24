package casa.security;

import java.util.List;

import casa.AbstractProcess;
import casa.MLMessage;
import casa.interfaces.PolicyAgentInterface;
import casa.interfaces.SecurityFilterInterface.EncryptionAlgorithm;

public class SecurityFilter implements casa.interfaces.SecurityFilterInterface {

	public SecurityFilter() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see casa.interfaces.SecurityFilterInterface#deleteEncrytpionAlgorithm(casa.interfaces.SecurityFilterInterface.EncryptionAlgorithm)
	 *
	 * @param alg
	 * @return
	 * @author kremer
	 */
	public boolean deleteEncrytpionAlgorithm(EncryptionAlgorithm alg) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see casa.interfaces.SecurityFilterInterface#getEncryptionAlgorithms()
	 *
	 * @return
	 * @author kremer
	 */
	public List<EncryptionAlgorithm> getEncryptionAlgorithms() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see casa.interfaces.SecurityFilterInterface#insertEncryptionAlgorithm(casa.interfaces.SecurityFilterInterface.EncryptionAlgorithm, int)
	 *
	 * @param alg
	 * @param location
	 * @return
	 * @author kremer
	 */
	public boolean insertEncryptionAlgorithm(EncryptionAlgorithm alg, int location) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see casa.interfaces.SecurityFilterInterface#processMessage(casa.MLMessage, casa.AbstractProcess, boolean)
	 *
	 * @param msg
	 * @param agent
	 * @param incoming
	 * @return
	 * @author kremer
	 */
	public MLMessage processMessage(MLMessage msg, PolicyAgentInterface agent, boolean incoming) {
		// TODO Auto-generated method stub
		return msg;
	}

	/**
	 * @see casa.interfaces.SecurityFilterInterface#requireIncomingSigning(boolean)
	 *
	 * @param required
	 * @return
	 * @author kremer
	 */
	public boolean requireIncomingSigning(boolean required) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see casa.interfaces.SecurityFilterInterface#setDefSignatureAlgorithm(java.lang.String)
	 *
	 * @param name
	 * @return
	 * @author kremer
	 */
	public boolean setDefSignatureAlgorithm(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see casa.interfaces.SecurityFilterInterface#setSecurityLevel(int)
	 *
	 * @param level
	 * @return
	 * @author kremer
	 */
	public int setSecurityLevel(int level) {
		// TODO Auto-generated method stub
		return 1;
	}

}
