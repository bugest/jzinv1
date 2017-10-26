package nc.ui.jzinv.receive;

import nc.ui.jzinv.pub.eventhandler.ManageEventHandler;
import nc.ui.jzinv.receive.action.ReceAddAction;
import nc.ui.jzinv.receive.action.ReceCancelAction;
import nc.ui.jzinv.receive.action.ReceEditAction;
import nc.ui.jzinv.receive.action.ReceRedAction;
import nc.ui.jzinv.receive.action.ReceSaveAction;
import nc.ui.pub.ButtonObject;
import nc.ui.pub.pf.PfUtilClient;
import nc.ui.trade.base.IBillOperate;
import nc.ui.trade.bill.RefBillTypeChangeEvent;
import nc.ui.trade.controller.IControllerBase;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.inv0503.ReceiveCollVO;
import nc.vo.jzinv.pub.IJzinvButton;
import nc.vo.jzinv.pub.utils.SafeCompute;
import nc.vo.jzinv.receive.AggReceiveVO;
import nc.vo.jzinv.receive.ReceiveDetailVO;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;

import org.apache.commons.lang.StringUtils;

public class ReceiveEH extends ManageEventHandler{

	public ReceiveEH(BillManageUI billUI, IControllerBase control) {
		super(billUI, control);
	}
	
	@Override
	public boolean isEnableMultiTablePRT(){
		return true;
	}
	
    @Override
	public void onBoAdd(ButtonObject bo) throws Exception{
    	setHeadItemEditable(true);
		super.onBoAdd(bo);
		new ReceAddAction((BillManageUI) getBillManageUI()).doAction();
	}
	@Override
	protected void onBoElse(int intBtn) throws Exception {
		super.onBoElse(intBtn);
		switch (intBtn) {
		case IJzinvButton.REFADDCOLLBTN:
			onBillRef(intBtn);
			break;
		case IJzinvButton.INV_RED:
			onRed();
		default:
			break;
		}
	}

	@SuppressWarnings("deprecation")
	private void onRed() throws Exception {
		ButtonObject bo = new ButtonObject("期初");
		super.onBoAdd(bo);
		new ReceRedAction(getBillManageUI()).doAction();
	}

	public void onBillRef(int intBtn) throws Exception {
		ButtonObject btn = getButtonManager().getButton(intBtn);
		btn.setTag(getBillUI().getRefBillType() + ":");
		onBoBusiTypeAdd(btn, null);

		btn.setTag(String.valueOf(intBtn));
		if (!PfUtilClient.isCloseOK()) {
			return;
		}else{
			getClientUI().setDefaultData();
			setHeadItemEditable(false);
		}
	}
	private ReceiveUI getClientUI() {
		return (ReceiveUI) getBillUI();
	}
	@Override
	protected boolean isDataChange() {
		return false;
	}
	private void setHeadItemEditable(boolean edit){
		String[] fields = new String[]{ReceiveVO.NINVMNY, ReceiveVO.NINVTAXMNY, ReceiveVO.NTAXMNY, ReceiveVO.NTAXRATE};
		for(String field : fields){
			getBillManageUI().getBillCardPanel().getHeadItem(field).setEdit(edit);
			if(field.equals(ReceiveVO.NTAXMNY)){
				continue;
			}
			getBillManageUI().getBillCardPanel().getHeadItem(field).setNull(true);
		}
	}
	private final void onBoBusiTypeAdd(ButtonObject bo, String sourceBillId)
			throws Exception {
				getBusiDelegator().childButtonClicked(bo, _getCorp().getPrimaryKey(),
						getBillUI()._getModuleCode(), _getOperator(),
						getUIController().getBillType(), getBillUI(),
						getBillUI().getUserObject(), sourceBillId);
				if (nc.ui.pub.pf.PfUtilClient.makeFlag) {
					// 设置单据状态
					getBillUI().setCardUIState();
					// 新增
					getBillUI().setBillOperate(IBillOperate.OP_ADD);
				} else {
					if (PfUtilClient.isCloseOK()) {
						if (getBillBusiListener() != null) {
							String tmpString = bo.getTag();
							int findIndex = tmpString.indexOf(":");
							String newtype = tmpString.substring(0, findIndex);
							RefBillTypeChangeEvent e = new RefBillTypeChangeEvent(this,
									null, newtype);
							getBillBusiListener().refBillTypeChange(e);
						}
						if (isDataChange())
							setRefData(PfUtilClient.getRetVos());
						else
							setRefData(PfUtilClient.getRetOldVos());

					} 
				}
	}
	@Override
	protected AggregatedValueObject refVOChange(AggregatedValueObject[] vos)
			throws Exception {
		if(null == vos || vos.length == 0){
			return null;
		}
		ReceiveCollVO collVO = (ReceiveCollVO) vos[0].getParentVO();
		ReceiveVO receHVO = new ReceiveVO();
		receHVO.setPk_project(collVO.getPk_project());//项目
		receHVO.setPk_projectbase(collVO.getPk_projectbase());//项目
		receHVO.setPk_supplier(collVO.getPk_supplier());//供应商(销售方)
		receHVO.setPk_supplierbase(collVO.getPk_supplier_base());//供应商
		receHVO.setPk_contract(collVO.getPk_contract());//合同
		receHVO.setDopendate(collVO.getDopendate());//开票日期
		receHVO.setDtodate(collVO.getDopendate());//票到日期
		receHVO.setVinvno(collVO.getVinvno());//发票号
		receHVO.setVinvcode(collVO.getVinvcode());//发票代码
		receHVO.setVtaxpayername(collVO.getVtaxpayername());//购方名称
		receHVO.setVtaxpayernumber(collVO.getVtaxpayerno());//购方纳税人识别号
		receHVO.setVtaxpayerphone(collVO.getVtaxpayeraddress());//购方地址电话
		receHVO.setVbankaccount(collVO.getVtaxpayeraccount());//购方开户银行及银行帐号
		receHVO.setVtaxsuppliernumber(collVO.getVtaxsupplieno());//销售方纳税人识别号
		receHVO.setVsupplierphone(collVO.getVtaxsupplieaddr());////销售方地址电话
		receHVO.setVsupbankaccount(collVO.getVtaxsupplieaccount());//销售方开户行及账号
		receHVO.setNinvmny(collVO.getNinvmny());//发票金额(无税)
		receHVO.setNinvtaxmny(collVO.getNinvtaxmny());//发票金额(含税)
		receHVO.setNoriginvmny(collVO.getNinvmny());//发票金额(原币无税)
		receHVO.setNoriginvtaxmny(collVO.getNinvtaxmny());//发票金额(原币含税)
		receHVO.setNtaxmny(collVO.getNtaxmny());//税额
		//税率
		UFDouble ntaxrate = SafeCompute.div(collVO.getNtaxmny(), collVO.getNinvmny());
		receHVO.setNtaxrate(SafeCompute.multiply(new UFDouble(100), ntaxrate));
		receHVO.setVlastbillid(collVO.getPk_receive_coll());//发票信息采集主键
		//供应商类型
		receHVO.setIsupplytype(collVO.getIsupplytype());
//		项目主键	vsecretcode
		receHVO.setVsecret(collVO.getVsecretcode());//密文
		
		AggReceiveVO aggVo = new AggReceiveVO();
		
		String pk_corp = getClientUI().getPk_corp();
		//IJzinvBillType.JZINV_RECEIVE_MT 应该取值各个收票的单据类型
		boolean bisupload = nc.vo.jzinv.param.InvParamTool.isToGLByBilltype(pk_corp, getClientUI().getUIControl().getBillType());
		receHVO.setBisupload(bisupload ? UFBoolean.TRUE : UFBoolean.FALSE);
		aggVo.setParentVO(receHVO);
		setBodyVO(aggVo);
		return aggVo;
	}
	
	private void setBodyVO(AggReceiveVO aggVo) {
		ReceiveVO hvo = (ReceiveVO) aggVo.getParentVO();
		ReceiveDetailVO detailVO = new ReceiveDetailVO();
		detailVO.setNtaxmny(hvo.getNtaxmny());
		detailVO.setNtaxrate(hvo.getNtaxrate());
		detailVO.setNthrecemny(hvo.getNinvmny());
		detailVO.setNthrecetaxmny(hvo.getNinvtaxmny());
		aggVo.setTableVO(ReceiveDetailVO.TABCODE, new ReceiveDetailVO[]{detailVO});
	}

	@Override
	protected void onBoLineAdd() throws Exception {
		super.onBoLineAdd();
		if(ReceiveDetailVO.TABCODE.equals(this.getBillCardPanelWrapper().getBillCardPanel().getCurrentBodyTableCode())) {
			UFDouble ntaxRate = new UFDouble((String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTAXRATE).getValueObject());
			int row = this.getClientUI().getBillCardPanel().getBillModel(ReceiveDetailVO.TABCODE).getRowCount();
			this.getClientUI().getBillCardPanel().setBodyValueAt(ntaxRate, row-1, ReceiveVO.NTAXRATE);
			String pk_project = (String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_PROJECT).getValueObject();
			String pk_projectbase = (String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_PROJECTBASE).getValueObject();
			
			this.getClientUI().getBillCardPanel().setBodyValueAt(pk_project, row-1, ReceiveVO.PK_PROJECT, ReceiveDetailVO.TABCODE);
			this.getClientUI().getBillCardPanel().setBodyValueAt(pk_projectbase, row-1, ReceiveVO.PK_PROJECTBASE, ReceiveDetailVO.TABCODE);
			this.getClientUI().getBillCardPanel().getBillModel(ReceiveDetailVO.TABCODE).execLoadFormula();
		}
		
	}
	@Override
	protected void onBoLineDel() throws Exception {
		super.onBoLineDel();
		
	}
	
	@Override
	protected void onBoLinePaste() throws Exception {
		super.onBoLinePaste();
		
	}
	
	@Override
	protected void onBoLinePasteToTail() throws Exception {
		super.onBoLinePasteToTail();
	
	}
	
	@Override
	protected void onBoSave() throws Exception {
		new ReceSaveAction((BillManageUI) getBillManageUI()).doAction();
		super.onBoSave();
	}
	
	@Override
	protected void onBoCancel() throws Exception {
		super.onBoCancel();
        new ReceCancelAction((BillManageUI) getBillManageUI()).doAction();
	}
	
	@Override
	protected void onBoEdit() throws Exception {
		String vlastbillid = (String) getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.VLASTBILLID).getValueObject();
		if(!StringUtils.isEmpty(vlastbillid)){
			setHeadItemEditable(false);
		}
		else{
			setHeadItemEditable(true);
		}
		//linan 20171025 根据单据状况设置税金拆分单可编辑性
		//setTaxSplitFields();
		super.onBoEdit();
	    new ReceEditAction((BillManageUI) getBillManageUI()).doAction();
	}
	
	@Override
	public void onBoAudit() throws Exception {
		super.onBoAudit();
		onBoRefresh();
	}
	
	@Override
	protected void onBoCancelAudit() throws Exception {
		super.onBoCancelAudit();
		onBoRefresh();
	}
}