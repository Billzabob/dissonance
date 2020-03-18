# O365 Print Tool 2.0

An attempt to recreate the [print tool](https://bitbucket.org/firelayers/o365-print/src/master/). Currently just works for events ([Management Activity API](https://docs.microsoft.com/en-us/office/office-365-management-api/office-365-management-activity-api-reference))

I wanted to use pure functional programming and some cool libraries I've been wanting to mess around with.

It runs fast and in constant memory thanks to fs2, so it won't freeze your computer 👍

Here's some links to the libraries used for the FP stack I wanted to mess with:

- [cats-effect](https://typelevel.org/cats-effect/)
- [fs2](https://fs2.io/)
- [http4s](https://http4s.org/v0.21/)
- [circe](https://circe.github.io/circe/)
- [refined](https://github.com/fthomas/refined#refined-simple-refinement-types-for-scala)
- [decline](http://ben.kirw.in/decline/)
- [newtype](https://github.com/estatico/scala-newtype#newtype)
- [enumeratum](https://github.com/lloydmeta/enumeratum#enumeratum------)
