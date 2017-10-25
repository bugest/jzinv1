package nc.ui.jzinv.receive;

import java.util.Hashtable;

import nc.ui.jzinv.pub.multichild.IMultiChildQueryInfo;
import nc.ui.jzinv.pub.multichild.JZFDCMultiChildBusinessDelegator;
import nc.vo.jzinv.pub.IMultiChildVOInfo;
import nc.vo.jzinv.pub.JZINVProxy;
import nc.vo.jzinv.receive.ReceiveBVO;
import nc.vo.jzinv.receive.ReceiveDetailVO;

public class ReceiveDelegator extends JZFDCMultiChildBusinessDelegator {
	
	private String[] m_TableCodes = new String[] {
			"jzinv_receive_b",
			"jzinv_receive_detail"
		};
	private static final Class[] classes = new Class[] {ReceiveBVO.class,ReceiveDetailVO.class};

	public Hashtable loadChildDataAry(String[] tableCodes, String key) throws Exception{
		return JZINVProxy.getJZUifService().loadChildDataAryAtJZ(classes,
				tableCodes, key);
	}

	private final String[] m_tableAlias = new String[]{
			"jzinv_receive_b","jzinv_receive_detail"
	};
	
	@Override
	public IMultiChildQueryInfo getMultiChildQueryInfo() {
		return new IMultiChildQueryInfo(){
			public String getAliasByTableCode(String tblCode){
				for(int i=0;i<m_TableCodes.length;i++){
					if(m_TableCodes[i].equalsIgnoreCase(tblCode)){
						return m_tableAlias[i];
					}
				}
				return null;
			}
		};
	}

	private String[] m_TableNames= new String[]{"收票信息子表","收票依据子表"};
	@Override
	public IMultiChildVOInfo getMultiChildVoInfo() {
		// TODO Auto-generated method stub

		return new IMultiChildVOInfo(){
			public String[] getVONames(){
				return m_TableNames;
			}
			public String getVoClassNameByTableCode(String tblCode){
				for(int i=0;i<m_TableCodes.length;i++){
					if(m_TableCodes[i].equalsIgnoreCase(tblCode))
						return m_TableNames[i];
				}
				return null;
			}

			public String[] getTableCodes(){
				return m_TableCodes;
			}
		};

	}
}