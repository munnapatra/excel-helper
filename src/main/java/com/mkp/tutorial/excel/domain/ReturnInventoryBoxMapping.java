package com.mkp.tutorial.excel.domain;

public class ReturnInventoryBoxMapping {

	private Integer boxId;
	private Integer storeNumericId;
	private Integer accountId;
	private Integer manufacturerID;
	private Integer manufacturerDivisionId;
	private Integer expiryRange;

	public Integer getBoxId() {
		return boxId;
	}

	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}

	public Integer getStoreNumericId() {
		return storeNumericId;
	}

	public void setStoreNumericId(Integer storeNumericId) {
		this.storeNumericId = storeNumericId;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public Integer getManufacturerID() {
		return manufacturerID;
	}

	public void setManufacturerID(Integer manufacturerID) {
		this.manufacturerID = manufacturerID;
	}

	public Integer getManufacturerDivisionId() {
		return manufacturerDivisionId;
	}

	public void setManufacturerDivisionId(Integer manufacturerDivisionId) {
		this.manufacturerDivisionId = manufacturerDivisionId;
	}

	public Integer getExpiryRange() {
		return expiryRange;
	}

	public void setExpiryRange(Integer expiryRange) {
		this.expiryRange = expiryRange;
	}

	@Override
	public String toString() {
		return "ReturnInventoryBoxMapping [boxId=" + boxId + ", storeNumericId=" + storeNumericId + ", accountId="
				+ accountId + ", manufacturerID=" + manufacturerID + ", manufacturerDivisionId="
				+ manufacturerDivisionId + ", expiryRange=" + expiryRange + "]";
	}

}