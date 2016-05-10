# addUsers OSGi Command for Liferay
Simple OSGi Module for batch-creating users for Liferay 7 (using randomuser.me)

Very simple but fast way to create as many random users for Liferay 7 as you wish. It uses the randomuser.me API to create users adding fields like name, email, picture and others.

Usage:

    - (connect to your local GOGO Console)
    - addUsers <companyID> -- Will add 1 sample user to the <companyID>
    - addUsers <num> <companyID> -- Will add <num> users to the <companyID>

### Version
0.0.1

### Tech
* [randomuser.me] - Cool API for creating powerful random users