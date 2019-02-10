package testing

abstract class GeneralMethod {
    var timesInvoked: Int = 0
        protected set
    lateinit var mostRecentInvokeArgument: Any
        protected set
    lateinit var mostRecentValueReturned: Any
        protected set
}