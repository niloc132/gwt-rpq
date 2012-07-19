### RPQ: Remote Procedure Queues

(Name subject to change)

The purpose of this project is to provide an automated mechanism for batching multiple GWT RPC calls into a single round trip HTTP request. 

#### Disadvantages over RPC, RequestFactory:

 *  Uses RPC serialization, slower than JSON
 *  Extra boilerplate like RF, with interfaces that only loosely map to their services

#### Advantages?

 *  Easy split points for all RPC related code (proxy, serializers)
 *  Same DTO on both client and server (no need for RF proxies)
 *  Easily batchable service calls, no need to carefully make edits only within a specific context
 *  Easy integration for server-side dependency injection for service instances

#### How do I use it?

 1. Understand GWT RPC, and its serialization rules
 2. Build async interfaces that declare the methods, parameters, return types, and allowed exceptions
 3. Create a RequestQueue subinterface that has a method for each async interface, and maps them to service types on the server
 4. GWT.create reusable instances of the RequestQueue - as methods are invoked, it will queue up these calls, and will make them all at once when RequestQueue.fire() is called.

Ideas for use (not all make sense when combined with other ideas):

 *  Wrap the RequestQueue so that when instances are requested, fire() is invoked in a scheduled command, either deferred or finally
 *  Wrap the fire() invocation in a GWT.runAsync split point to move all RPC serializers into a separate split point
 *  Allow several instances of a RequestQueue so that some parts of the app can queue up changes to be invoked, while others can run right away if necessary (all save() calls might run when the user clicks save, while other parts of the app might need to load data right away for autocomplete or paging).

Ideas currently on the roadmap:

 *  AutoBean support instead of just RPC, to allow for JSON serialization
 *  RF-style ServiceLocator instances
 *  Built-in polling support, allowing server continuations (relying on the queue not behaving like a RF request context)
 *  Built-in split point, scheduling generation
