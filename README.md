MastodonFeed Android App

Here is the implementation of the Android coding task, about reading a public Mastodon feed.
Firstly, I created an account for the Mastodon API, in order to gain access to their API. I have used your suggested mas.to instance.
To communicate with the api, I have discovered and used the BigBone library, which was in the list of libraries on the main Mastodon website.
Their Github page is here https://github.com/andregasser/bigbone
The reason I added this library, is to avoid spending time creating the communication with the api from scratch, as well as the required models,
api callbacks, errors, etc.
I have isolated the library implementation into its own module (api). The actual implementation of the api is done in the MastodonServiceImpl.
This allows for easier change of the library in the future if needed.
I have also created another module for logging, again to help with isolating the logging functionality. Any logging to a remote server, for
example logging errors for analytics purposes can be done there, using a different implementation.
Ideally I would also create another module to separate the Internet Connectivity check functionality, as it could be used in other modules, 
as well as a TimeProvider module.
Another module could be created for the architecture, used in the ViewModel, so it can be reused and managed separately.

I have spent a big amount of time, creating the ViewModel, trying to showcase some ideas, on how to separate different functionalities into
their own classes and flows. The idea is that one thread is being used to process the Inputs of the ViewModel, which basically are 
inputs from the user, use cases, other events as flows, etc. Being processed into one thread, separately from the UI thread, makes the UI
free of Jank, and allows us to freely do any data processing we need. Of course, if any more computational heavy workload is needed, it could
be done on a different thread, using computational dispatchers.
The inputs contain the transform function, which transforms the existing model to the next, according to the input at that time. For example, 
whenever a new FeedItem is emitted, then the state will be updated to include it into all of its items, as well as update the filtered items 
list.

I have added some unit tests as well, in different types of classes, in order to show different scenarios of unit tests.
Unit testing this type of ViewModel was not very straight forward, but there could be solutions like, using composition to modularise it and 
inject the different parts of its functionality, which will help in the future if any changes would be needed.

I hope you find my idea of flows in ViewModels interesting. I would love if you have any feedback regarding it and also about your ideas 
and solutions regarding this section of Android apps.
Looking forward to chatting.

Many thanks!