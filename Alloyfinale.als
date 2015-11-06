module MyTaxi

//SIGNATURES


sig Request{
	client: one Guest,
	taxi: one TaxiDriver,
	zoneQueue: one Queue
}

sig Reservation{
	client: one User,
	generatedRequest: one Request
}

sig Guest{
	request: lone Request
}

sig User extends Guest{
	reservation: lone Reservation
}

sig TaxiDriver{
	actualRequest: lone Request,
	actualQueue: lone Queue  
}

sig Queue{
	queuedTaxis: set TaxiDriver
}


//FACTS

//request facts
fact{
	client = ~request
	actualRequest = ~taxi

//every taxi associated to a request must be in the queue selected by the request
	all q: Queue, t: TaxiDriver, r: Request | t in q.queuedTaxis and r.taxi = t
		implies r.zoneQueue=q

//every user who made a reservation must be also the client of the generated request
	all r: Request, u: User, s: Reservation | s.client = u and s.generatedRequest = r
		implies r.client=u
}

//queue facts
fact{
	actualQueue = ~queuedTaxis
//the taxi driver that accept a request have to be listed in the actual queue identified by the request itself
	all q:Queue, t:TaxiDriver, r:Request | r.taxi=t and r.zoneQueue=q
		implies t in q.queuedTaxis
}

//reservation facts
fact{
	client = ~reservation

//only at most one reservation for every request
	all b,b': Reservation | b.generatedRequest = b'.generatedRequest
		implies b = b'
}


pred showRequests {
	#User = 3
	#TaxiDriver = 10
	
}

pred show1{
	#Queue=1
	#Reservation=0
	#Guest > 0
	#User > 0	
}


pred show2{
	#Queue=1
	#Reservation=1
	#Guest > 0
	#User > 0		
}



run show2 for 3
