package com.example.cosmetologistmanager

class ListReportData(
    var name: String,
    var day: String,
    var month: String,
    var year: String,
    var price: String,
    val kind: OperationKind? = OperationKind.Expense,
    val hash: String,
)
enum class OperationKind {
    Expense, Income
}