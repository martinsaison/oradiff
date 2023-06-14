package com.khodev.oradiff.xml

data class DBParameter(
    val name: String,
    val id: Int,
    val type: String,
    val length: Int,
    val precision: Int,
    val scale: Int,
    val nullable: Boolean,
    val comments: String?,
    val defaultValue: String?
)

data class DBFunction(
    val name: String,
    val type: String,
    val status: String,
    val comments: String?,
    val grants: List<DBGrant>,
    val parameters: List<DBParameter>
)

data class DBProcedure(
    val name: String,
    val type: String,
    val status: String,
    val comments: String?,
    val grants: List<DBGrant>,
    val parameters: List<DBParameter>
)

data class DBJob(
    val name: String,
    val schema: String,
    val jobType: String,
    val jobAction: String,
    val jobSchedule: String,
    val comments: String?
)

data class DBSequence(
    val name: String,
    val minValue: Int,
    val maxValue: Int,
    val incrementBy: Int,
    val cycle: Boolean,
    val order: Boolean,
    val comments: String?
)

data class DBSynonym(
    val name: String,
    val owner: String,
    val tableOwner: String,
    val tableName: String,
    val dbLink: String?,
    val comments: String?
)

data class DBPackage(
    val name: String,
    val type: String,
    val status: String,
    val comments: String?,
    val grants: List<DBGrant>,
    val functions: List<DBFunction>,
    val procedures: List<DBProcedure>
)

data class DBColumn(
    val name: String,
    val id: Int,
    val type: String,
    val length: Int,
    val precision: Int,
    val scale: Int,
    val nullable: Boolean,
    val comments: String?,
    val defaultValue: String?
)

data class DBIndex(
    val name: String,
    val tablespace: String,
    val columns: List<String>,
    val unique: Boolean,
    val reverse: Boolean,
    val comments: String?
)

data class DBConstraint(
    val name: String,
    val type: String,
    val columns: List<String>,
    val rOwner: String?,
    val rTable: String?,
    val rColumns: List<String>?,
    val deleteRule: String?,
    val status: String?,
    val searchCondition: String?,
    val comments: String?
)

data class DBGrant(
    val grantor: String,
    val grantee: String,
    val privilege: String,
    val grantable: Boolean
)

data class DBPublicSynonym(
    val name: String,
    val tableOwner: String,
    val tableName: String,
    val comments: String?
)

data class DBTable(
    val owner: String,
    val tableName: String,
    val tablespace: String,
    val comments: String?,
    val columns: List<DBColumn>,
    val indexes: List<DBIndex>,
    val constraints: List<DBConstraint>,
    val grants: List<DBGrant>,
    val publicSynonyms: List<DBPublicSynonym>
)

data class DBView(
    val name: String,
    val source: String
)

data class DBTrigger(
    val name: String,
    val type: String,
    val event: String,
    val table: String,
    val whenClause: String,
    val status: String,
    val description: String,
    val body: String
)

data class DBSchema(
    val name: String,
    val functions: List<DBFunction>,
    val procedures: List<DBProcedure>,
    val jobs: List<DBJob>,
    val sequences: List<DBSequence>,
    val synonyms: List<DBSynonym>,
    val packages: List<DBPackage>,
    val tables: List<DBTable>,
    val views: List<DBView>,
    val triggers: List<DBTrigger>
)

data class Database(
    val schemas: List<DBSchema>
)