module MyTaxi

//SIGNATURES


sig Request{
	client: one User,
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
}

//reservation facts
fact{
	client = ~reservation

//only at most one reservation for every request
	all b,b': Reservation | b.generatedRequest = b'.generatedRequest //deus ex machina
		implies b = b'
}


pred show {}

run show for 4
