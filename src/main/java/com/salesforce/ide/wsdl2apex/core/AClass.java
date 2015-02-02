/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc.. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.wsdl2apex.core;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import com.sforce.ws.wsdl.Definitions;

/**
 * AClass
 * 
 * @author cheenath
 * @version 1.0
 */
abstract class AClass extends ABase {

    protected final String extendsClass;
    protected final String name;
    protected final ArrayList<AMethod> methods = new ArrayList<AMethod>();
    protected final AClass superClass;
    protected boolean isVirtual;
    public ArrayList<AField> fields = new ArrayList<AField>();
    public ArrayList<AField> subclassFields = new ArrayList<AField>();

    protected AClass(com.sforce.ws.wsdl.Definitions definitions, ApexTypeMapper typeMapper, String name, AClass superClass){
        this(definitions,typeMapper,name,superClass,null);
    }

    protected AClass(Definitions definitions, ApexTypeMapper typeMapper, String name,AClass superClass,String extendsClass) {
        super(definitions, typeMapper);
        this.name = name;
        this.superClass = superClass;
        this.extendsClass = extendsClass;
    }

    public String getName() {
        return name;
    }

    public void setIsVirtual(boolean isVirtual){
        this.isVirtual = isVirtual;
    }

    /**
     * Adds a field to this class if one does not already exist with the same name and access.
     * @param fields [description]
     */
    public void addSubclassFields(ArrayList<AField> fields){
        this.subclassFields.addAll(fields);
    }

    public void addField(AField field){
        this.fields.add(field);
    }

    public void addFields(ArrayList<AField> fields){
        this.fields.addAll(fields);
    }

    public ArrayList<AField> getFields(){
        return this.fields;
    }

    public ArrayList<AField> getSubclassFields(){
        return this.subclassFields;
    }

    void write(AWriter writer) throws CalloutException {
        writer.startBlock();

        String extendsClass = null;
        if(this.superClass != null){
            extendsClass = superClass.getName();
        }else{
            extendsClass = this.extendsClass;
        }

        writer.writeLine(
            "public"
            ,isVirtual ? " virtual class " : " class "
            ,name
            ,extendsClass == null ? " {" : " extends "
            ,extendsClass == null ? "" : extendsClass
            ,extendsClass == null ? "" : " {"
        );

        Set<String> fieldsOutputted = new HashSet<String>();

        for (AField field : this.fields) {
            if(!fieldsOutputted.contains(field.getName().toLowerCase())){
                if (field.isPublic()) {
                    field.write(writer);
                    fieldsOutputted.add(field.getName().toLowerCase());
                }
            }
        }

        for(AField field : this.subclassFields){
            if(!fieldsOutputted.contains(field.getName().toLowerCase())){
                if (field.isPublic()) {
                    field.write(writer);
                    fieldsOutputted.add(field.getName().toLowerCase());
                }
            }
        }

        AField fieldOrderInfo = null;
        for (AField field : this.fields) {
            if(!fieldsOutputted.contains(field.getName().toLowerCase())){
                if(!field.isPublic()){
                    if(!field.getName().equalsIgnoreCase(CalloutConstants.FIELD_ORDER_INFO)){
                        field.write(writer);
                        fieldsOutputted.add(field.getName().toLowerCase());
                    }else{
                        fieldOrderInfo = field;
                    }
                }
            }
        }

        for(AField field : this.subclassFields){
            if(!fieldsOutputted.contains(field.getName().toLowerCase())){
                if (!field.isPublic()) {
                    field.write(writer);
                    fieldsOutputted.add(field.getName().toLowerCase());
                }
            }
        }

        //FIELD_ORDER_INFO
        if(fieldOrderInfo != null){
            fieldOrderInfo.write(writer);
        }

        for (AMethod method : methods) {
            method.write(writer);
        }

        writer.writeLine("}");
        writer.endBlock();
    }
}