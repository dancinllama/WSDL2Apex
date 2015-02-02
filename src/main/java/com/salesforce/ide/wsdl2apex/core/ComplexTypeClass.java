/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc.. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.wsdl2apex.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import com.sforce.ws.ConnectionException;
import com.sforce.ws.wsdl.*;

/**
 * This generates an Apex class that represents a complexType (aka struct)
 */
class ComplexTypeClass extends AClass {

    ComplexTypeClass(ComplexType ct, Definitions definitions, ApexTypeMapper typeMapper) throws CalloutException,
            ConnectionException {
        this(ct,definitions, typeMapper, null);
    }

    ComplexTypeClass(ComplexType ct, Definitions definitions, ApexTypeMapper typeMapper, ComplexTypeClass superClass) throws CalloutException,
            ConnectionException {
        super(definitions, typeMapper, typeMapper.getSafeName(ct.getName()), superClass);

        ArrayList<String> fieldOrder = new ArrayList<String>();
        Collection sequence = ct.getContent();
        if (sequence != null) {
            Iterator<Element> elements = ct.getContent().getElements();
            while (elements.hasNext()) {
                Element element = elements.next();
                if (element.getName() == null) {
                    throw new CalloutException("Unable to find name for element in :" + ct.getName());
                }
                String name = typeMapper.getSafeName(element.getName());
                String apexType = typeMapper.getApexType(element, definitions).getAsApex();
                fields.add(new AField("public", apexType, name, null)); //access, type, name, value
                fields.add(new AField("private", "String[]", name + CalloutConstants.TYPE_INFO_PREFIX,
                        typeInfo(element)));
            }
        }

        Iterator<Attribute> attributesIt = ct.getAttributes();
        while (attributesIt.hasNext()) {
            Attribute attribute = attributesIt.next();
            String name = typeMapper.getSafeName(attribute.getName());
            ApexTypeName apexType = typeMapper.getApexType(attribute.getType(), definitions);
            if (apexType == null) {
                throw new CalloutException("Unable to find type from attribute: " + name);
            }
            if (apexType.hasPackageName()) {
                throw new CalloutException("Attribute ' " + name + "' cannot use complexType: " + apexType);
            }
            fields.add(new AField("public", apexType.getAsApex(), name, null));

            fields.add(new AField("private", "String[]", name + CalloutConstants.ATTRIBUTE_INFO_PREFIX,
                    attributeInfo(attribute)));
        }

        if(superClass != null){
            superClass.addSubclassFields(fields);
            fields.addAll(superClass.getFields());
        }

        addComplexTypeInfo(ct);


        fieldOrder = new ArrayList<String>();
        for(AField field : fields){
            if(field.isPublic()){
                fieldOrder.add(field.getName());
            }
        }
        addFieldOrderInfo(fieldOrder,this);

        if(superClass != null){
            System.out.println("JWL super class is not null!");
            System.out.println("JWL super class name: " + superClass.getName());
            fieldOrder = new ArrayList<String>();
            for(AField field : superClass.getFields()){
                if(field.isPublic()){
                    fieldOrder.add(field.getName());
                }
            }
            for(AField field : superClass.getSubclassFields()){
                if(field.isPublic()){
                    fieldOrder.add(field.getName());
                }
            }
            System.out.println("JWL: fieldOrder size: " + fieldOrder.size());
            addFieldOrderInfo(fieldOrder,superClass);
        }
    }

    private String attributeInfo(Attribute attribute) {
        StringBuilder sb = new StringBuilder();
        sb.append("new String[]{");
        sb.append("'").append(attribute.getName()).append("'");
        sb.append("}");
        return sb.toString();
    }

    private String typeInfo(Element element) {
        String elemNs =
                element.getRef() == null ? element.getSchema().getTargetNamespace() : element.getRef()
                        .getNamespaceURI();

        StringBuilder sb = new StringBuilder();
        sb.append("new String[]{");
        sb.append("'").append(element.getName()).append("'");
        sb.append(",'").append(elemNs).append("'");
        sb.append(",").append("null"); // Was the element's type, no longer used.
        sb.append(",'").append(element.getMinOccurs()).append("'");
        sb.append(",'").append(element.getMaxOccurs()).append("'");
        sb.append(",'").append(element.isNillable()).append("'");
        sb.append("}");
        return sb.toString();
    }

    private void addFieldOrderInfo(ArrayList<String> fieldOrder,AClass aClassInstance) {

        Set<String> fieldsToIgnore = new HashSet<String>();
        fieldsToIgnore.add(CalloutConstants.FIELD_ORDER_INFO);
        StringBuilder sb = new StringBuilder();
        sb.append("new String[]{");
        for (int i = 0; i < fieldOrder.size(); i++) {
            if(!fieldsToIgnore.contains(fieldOrder.get(i).toLowerCase())){
                sb.append("'").append(fieldOrder.get(i)).append("'");
                sb.append(",");
                fieldsToIgnore.add(fieldOrder.get(i).toLowerCase());
            }
        }
        sb.append("}");

        System.out.println("JWL: filedOrderInfo for " + aClassInstance.getName() + " : " + sb.toString());

        aClassInstance.addField(
            new AField(
                "private"
                , "String[]"
                , CalloutConstants.FIELD_ORDER_INFO
                , sb.toString().replace(",}","}")
            )
        );
    }

    private void addComplexTypeInfo(ComplexType ct) {
        StringBuilder sb = new StringBuilder();
        sb.append("new String[]{");
        sb.append("'").append(ct.getSchema().getTargetNamespace()).append("'");
        sb.append(",'").append(ct.getSchema().isElementFormQualified()).append("'");
        sb.append(",'").append(ct.getSchema().isAttributeFormQualified()).append("'");
        sb.append("}");
        fields.add(new AField("private", "String[]", CalloutConstants.APEX_SCHEMA_INFO, sb.toString()));
    }
}