## Contributing to Eclipse Tractus-X
### Project description
Within the [Catena-X network](https://catena-x.net/), the so-called Item Relationship Service (IRS) forms an essential
foundation for various services and products. Within the Catena-X use cases, the IRS serves to increase business value.
For example, the IRS provides functionalities to serve requirements, such as occasion-based Traceability,
from the Supply Chain Act. In doing so, IDSA and Gaia-X principles, such as data interoperability and sovereignty, are
maintained on the Catena-X network and access to dispersed data is enabled. Data chains are established as a common asset.

With the help of the IRS, data chains are to be provided ad-hoc across n-tiers within the Catena-X network.
To realize these data chains, the IRS relies on data models of the Traceability use case and provides the federated
data chains to customers or applications. Furthermore, the target picture of the IRS includes the enablement of new
business areas by means of data chains along the value chain in the automotive industry.
### Developer resources
In progress

Information regarding source code management, builds, coding standards, and more.
[Developer Resources](https://projects.eclipse.org/projects/automotive.tractusx/developer)

The project maintains the source code repositories in the following GitHub organization:
[Eclipse Tractusx](https://github.com/eclipse-tractusx/)

### Eclipse Development Process
This Eclipse Foundation open project is governed by the Eclipse Foundation Development Process and operates under the terms of the Eclipse IP Policy.
[Eclipse Foundation Development Process](https://eclipse.org/projects/dev_process)

[Eclipse Intellectual Property Policy](https://www.eclipse.org/org/documents/Eclipse_IP_Policy.pdf)

### Eclipse Contributor Agreement
In order to be able to contribute to Eclipse Foundation projects you must electronically sign the Eclipse Contributor Agreement (ECA).
[Eclipse Contributor Agreement](http://www.eclipse.org/legal/ECA.php)

The ECA provides the Eclipse Foundation with a permanent record that you agree that each of your contributions will comply with the commitments documented in the Developer Certificate of Origin (DCO). Having an ECA on file associated with the email address matching the "Author" field of your contribution's Git commits fulfills the DCO's requirement that you sign-off on your contributions.

For more information, please see the [Eclipse Committer Handbook](https://www.eclipse.org/projects/handbook/#resources-commit) 


### Commit messages
The commit messages have to match a pattern in the form of:  
< type >(optional scope):[<Ticket_ID>] < description >

Example:  
chore(api):[TRI-123] some text

Detailed pattern can be found here: [commit-msg](dev/commit-msg)


### Style guides
Generally, the IRS application follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
The file [checkstyle](https://github.com/catenax-ng/product-item-relationship-service/tree/main/ci) configures 
which modules to plug in and apply to the IRS java source code. The IRS built fails in case 
of checkstyle violations.

### Found a bug what shall I do?
In case of a found bug in the IRS application, a bug task on jira should be created and handled 
by a developer.
