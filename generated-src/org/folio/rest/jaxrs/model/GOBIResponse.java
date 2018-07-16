package org.folio.rest.jaxrs.model;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="PoLineNumber" type="{}PoLineNumber"/>
 *         &lt;element name="Error" type="{}ResponseError"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "poLineNumber", "error" })
@XmlRootElement(name = "Response")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:35:01-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
public class GOBIResponse {

    @XmlElement(name = "PoLineNumber")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:35:01-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String poLineNumber;

    @XmlElement(name = "Error")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:35:01-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected ResponseError error;

    /**
     * Gets the value of the poLineNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:35:01-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public String getPoLineNumber() {
        return poLineNumber;
    }

    /**
     * Sets the value of the poLineNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:35:01-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setPoLineNumber(String value) {
        this.poLineNumber = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseError }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:35:01-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public ResponseError getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseError }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:35:01-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setError(ResponseError value) {
        this.error = value;
    }
}
