package script.db

databaseChangeLog(logicalFilePath: 'state_machine_lookup_value.groovy') {
    changeSet(id: '2018-09-26-state-machine-lookup-value', author: 'fuqianghuang01@gmail.com') {
        createTable(tableName: "state_machine_lookup_value", remarks: '快码表') {
            column(name: 'value_code', type: 'VARCHAR(255)', remarks: '主键') {
                constraints(primaryKey: true)
            }
            column(name: 'type_code', type: 'VARCHAR(255)', remarks: '类型code') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '名称') {
                constraints(nullable: false)
            }
            column(name: 'description', type: 'VARCHAR(255)', remarks: '描述')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(indexName: 'uk_value_code_type_code', tableName: 'state_machine_lookup_value', unique: true) {
            column(name: 'value_code')
            column(name: 'type_code')
        }
    }
}