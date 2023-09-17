# LITIENGINE contribution guide
You are awesome! Thank you for considering contributing to LITIENGINE.
Before you start, we would like to share a few guidelines for contributions to LITIENGINE with you.

In order to contribute to our project, we expect you to have fully read and agreed to our [Code of Conduct](https://github.com/gurkenlabs/LITIENGINE/blob/master/CODE_OF_CONDUCT.md). 

## How can I help?
You can not only support the development of LITIENGINE by contributing code!
There are tons of ways to help us maintain the project and keep the community vivid and helpful, 
a few of which we want to mention here:

### You like organizing events?

* Organize LITIENGINE workshops, meetups or game jams
* Help us establish a regular LITIENGINE conference
* Help community members find the right conferences and submit proposals for speaking

### You like designing stuff?

* Improve LITIENGINE’s usability by generating class diagrams
* Maximize the utiLITI editor's usability by redesigning UI layouts
* Help us establishing a style guide to maintain consistency in LITIENGINE's code
* Submit LITIENGINE related art to be used on merchandise products

### You like writing?

* Improve coverage in LITIENGINE's documentation
* Start a newsletter for LITIENGINE
* Write tutorials, How-To's, and user guides for LITIENGINE
* Translate LITIENGINE’s documentation

### You like structuring things?

* Link to duplicate issues and suggest new issue labels
* Go through open issues and suggest closing old ones
* Ask clarifying questions on recently opened issues to move the discussion forward

### You like coding?
* Find [open issues](https://github.com/gurkenlabs/litiengine/issues) and resolve them
* Implement [new features](https://github.com/gurkenlabs/litiengine/issues?q=is%3Aopen+is%3Aissue+label%3Afeature)
* Automate project setup
* Improve tooling and testing
* Enhance LITIENGINE workflows by improving the utiLITI editor.

### You like being helpful?
* Answer questions about LITIENGINE on e.g., our forum, Stack Overflow or Reddit
* Answer questions on open issues
* Become a moderator for our Github repository or the [LITIENGINE forum](https://forum.LITIENGINE.com/)
* Review code on other people’s submissions
* Support new contributors and mentor them

### Nothing of the above applies to you?
Well... if you still want to support us, you could consider helping us bear the development costs for LITIENGINE (e.g. servers and equipment).
You can do so by sponsoring [LITIENGINE on OpenCollective](https://opencollective.com/litiengine) or sponsoring the devs [Steffen](https://github.com/sponsors/steffen-wilke) and [Matthias](https://github.com/sponsors/nightm4re94) directly on GitHub.

## Code contributions
### Setting up your development environment
* [Install JDK 17](https://litiengine.com/docs/getting-started/install-jdk/)
* [Clone the LITIENGINE repository](https://github.com/gurkenlabs/litiengine.git)
* Open the LITIENGINE project in [your IDE of choice](https://litiengine.com/docs/getting-started/development-environment/)
* Make sure that the project is imported as a Gradle project
* Make sure that the project is compiled with JDK 17 and that JDK 17 is also set as your Gradle runtime
* Use Gradle to build the project. You can do this via the UI, or open a terminal in the project's root folder and type `./gradlew build`.
* If the build was successful, you have now completed all steps required for contributing to LITIENGINE development. Continue with the next section.
* Having any trouble? Ask for help in the [GitHub discussions](https://github.com/gurkenlabs/litiengine/discussions) or on our [Discord server](https://discord.com/invite/rRB9cKD).

### Submitting code contributions
* Create a GitHub account
* Fork the [LITIENGINE repository](https://github.com/gurkenlabs/litiengine)
* Create a thematically labelled branch
* Commit & Push your changes to the branch
  * Commits should be logical and atomic units. Commit often!
  * We expect any new methods that you write to be fully documented
  * Make sure to also include unit tests for new features
  * Clean up your code following our style guide below.
* File a Pull Request
  * We try to review pull requests as often as possible, but don't be mad if it takes us some days before reviewing yours.
  * As soon as your changes were reviewed and approved, we will merge them into the master branch.
  * Due to questionable design decisions, violations of our code of conduct, or bugs, we may reject pull requests and ask for changes. Again, this is nothing to be mad about: After you've revised the defective portions of your pull request, we'll happily review it once more.
  * Changes resulting in build and test pipeline failures that cannot be resolved within two days will be reverted.

### Style Guide
Code in the LITIENGINE repository needs to be formatted in compliance with the **gurkenlabs Java code style**. You can use the configuration file provided in [`config/gurkenlabs.eclipseformat.xml`](config/gurkenlabs.eclipseformat.xml).
The configuration can be imported for automatic formatting in Eclipse (`Window -> Preferences -> Java -> Code Style -> Formatter`) and IntelliJ (`File → Settings → Editor → Code Style`).
To ensure compliance with our code style when raising Pull Requests, use the provided Gradle tasks `spotlessCheck`, `spotlessApply`, and `spotlessDiagnose`.

## What else can I do?
>  If you want to help in other ways we have not mentioned above, just contact us at info@LITIENGINE.com to specify the details.

