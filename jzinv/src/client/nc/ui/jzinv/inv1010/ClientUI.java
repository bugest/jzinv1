package nc.ui.jzinv.inv1010;

import nc.bs.logging.Logger;
import nc.itf.jzinv.pub.IJZPMPubBusi;
import nc.ui.jzinv.pub.ref.SubReceiveRefModel;
import nc.ui.jzinv.pub.refmodel.ContractActiveRefTreeModel;
import nc.ui.jzinv.receive.ReceiveUI;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillItemEvent;
import nc.ui.trade.bill.AbstractManageController;
import nc.ui.trade.manage.ManageEventHandler;
import nc.vo.bd.basedata.CurrtypeVO;
import nc.vo.jzinv.cmpub.IContConst;
import nc.vo.jzinv.pub.JZINVProxy;
import nc.vo.jzinv.receive.ReceiveDetailVO;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.BusinessException;

import org.apache.commons.lang.StringUtils;

/**
 * 分包收票
 * @author mayyc
 *
 */
public class ClientUI extends ReceiveUI{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ContractActiveRefTreeModel contraRefModel;
	private ContractActiveRefTreeModel bodyContraRefModel;
    private SubReceiveRefModel blueRefModel;
	public ClientUI() {
		super();
		initRefModel();
	}
	private void initRefModel(){
		contraRefModel = new ContractActiveRefTreeModel(new int[] {IContConst.CONTTYPE_1_SUBCONT});
		bodyContraRefModel = new ContractActiveRefTreeModel(new int[] {IContConst.CONTTYPE_1_SUBCONT});
		try {
			int curdigit = getCurtypedigit();
			blueRefModel = new SubReceiveRefModel(curdigit, new String[]{ReceiveVO.NINVMNY, ReceiveVO.NINVTAXMNY}, 0, null);
		} catch (BusinessException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected AbstractManageController createController() {
		return new ClientCtrl();
	}
	
	@Override
	protected ManageEventHandler createEventHandler() {
		return new EventHandler(this, getUIControl());
	}
	/**
	 * 表头编辑前事件
	 */
	@Override
	public boolean beforeEdit(BillItemEvent e) {
		if(e.getItem().getPos() == BillItem.HEAD){
			if (ReceiveVO.PK_CONTRACT.equals(e.getItem().getKey())) {
				BillItem item = this.getBillCardPanel().getHeadItem(ReceiveVO.PK_CONTRACT);
				UIRefPane panel = (UIRefPane) item.getComponent();
				panel.setRefModel(contraRefModel);
			}
			else if(ReceiveVO.PK_RECEIVE_REF.equals(e.getItem().getKey())){
				blueBeforeEdit(e);
			}
		}
		return super.beforeEdit(e);
	}
	/**
	 * 对应蓝字发票编辑前事件
	 * @param e
	 */
	private void blueBeforeEdit(BillItemEvent e){
		StringBuffer where = new StringBuffer();
		String pk_receive = (String) this.getBillCardPanel().getHeadItem(ReceiveVO.PK_RECEIVE).getValueObject();
		String pk_project = (String) this.getBillCardPanel().getHeadItem(ReceiveVO.PK_PROJECT).getValueObject();
		String pk_supplier = (String) this.getBillCardPanel().getHeadItem(ReceiveVO.PK_SUPPLIER).getValueObject();
		UIRefPane refPane = (UIRefPane) this.getBillCardPanel().getHeadItem(ReceiveVO.PK_RECEIVE_REF).getComponent();
		if(!StringUtils.isEmpty(pk_project)){
			where.append(" and pk_project='").append(pk_project).append("'");
		}
		if(!StringUtils.isEmpty(pk_supplier)){
			where.append(" and pk_supplier='").append(pk_supplier).append("'");
		}
		if(!StringUtils.isEmpty(pk_receive)){
			where.append(" and pk_receive !='").append(pk_receive).append("'");
		}
		where.append(" and bisred='N' ");
		where.append(" and dr = 0 and vbillstatus = 1 and (isnull(ninvmny,0)-isnull(nhadredmny,0)) > 0 ");
		blueRefModel.addWherePart(where.toString());
		refPane.setRefModel(blueRefModel);
	}
	/**
	 * 表体编辑前事件
	 */
	@Override
	public boolean beforeEdit(BillEditEvent e){
		if(e.getPos() == BillItem.BODY){
			if (ReceiveDetailVO.VCONTRACTCODE.equals(e.getKey())) {
				contractCodeBodyBeforeEdit(e);
			}		
		}
		 return super.beforeEdit(e);
	}
	/**
	 * 表体合同编码编辑前事件
	 * @param e
	 */
	private void contractCodeBodyBeforeEdit(BillEditEvent e){
		if (ReceiveDetailVO.VCONTRACTCODE.equals(e.getKey())) {
			BillItem item = this.getBillCardPanel().getBodyItem(
					ReceiveDetailVO.TABCODE, ReceiveDetailVO.VCONTRACTCODE);
			// 根据项目、供应商过滤
			String pk_project = (String) getBillCardPanel().getHeadItem(
					ReceiveVO.PK_PROJECT).getValueObject();
			String pk_supplier = (String) getBillCardPanel().getHeadItem(
					ReceiveVO.PK_SUPPLIER).getValueObject();
			String pk_contract = (String) getBillCardPanel().getHeadItem(
					ReceiveVO.PK_CONTRACT).getValueObject();
			if (StringUtils.isEmpty(pk_supplier)) {
				try {
					throw new BusinessException("请先选择供应商!");
				} catch (BusinessException e1) {
					Logger.error(e1.getMessage());
				}
			}
			if (StringUtils.isEmpty(pk_project)) {
				try {
					throw new BusinessException("请先选择项目!");
				} catch (BusinessException e1) {
					Logger.error(e1.getMessage());
				}
			}
			bodyContraRefModel.setExtWherePart(null);
			bodyContraRefModel.setExtWherePart(getContRefWherePart(pk_project,pk_supplier, pk_contract));
			UIRefPane panel = (UIRefPane) item.getComponent();
			panel.setRefModel(bodyContraRefModel);
		}
	}
	private String getContRefWherePart(String pk_project, String pk_supplier, String pk_contract) {
		StringBuffer wherePart = new StringBuffer();
		if(pk_project != null) {
			wherePart.append(" and pk_project = '").append(pk_project).append("'");
		}
		if(pk_supplier != null) {
			wherePart.append(" and pk_second = '").append(pk_supplier).append("'");
		}
		if (!StringUtils.isEmpty(pk_contract)) {
			wherePart.append(" and pk_contract = '"
					+ pk_contract + "' ");					
		}
		return wherePart.toString();
	}
	private int getCurtypedigit() throws BusinessException{
		IJZPMPubBusi busiService = JZINVProxy.getIJZPMPubBusi();
		String where = " currtypecode = 'CNY' and isnull(dr, 0)=0 ";
		CurrtypeVO[] curtypeVOs = (CurrtypeVO[])busiService.queryByWhereClause(CurrtypeVO.class, where, null, new String[]{"currdigit"});
		
		if(null != curtypeVOs){
			return curtypeVOs[0].getCurrdigit();
		}
		return 0;
	}
}
