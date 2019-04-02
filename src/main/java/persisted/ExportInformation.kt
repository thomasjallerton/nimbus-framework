package persisted

data class ExportInformation(
        val exportName: String,
        val exportMessage: String,
        val substitutionVariable: String
)