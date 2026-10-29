---
title: Kakao Login broke, but every key hash was registered
summary: If Kakao Login suddenly fails in a shipped app, check for a legacy SHA-1 fingerprint in Play Console that you never registered.
source: a8dc5cbaf393
---

A user recently left a review on one of my already-released apps saying Kakao Login (the social login from Kakao, Korea's biggest messenger) had suddenly stopped working.

![](images/01.webp){width=100 align=center}

That puzzled me. The app had gone through verification, and I'd registered pretty much every key hash I could think of in Kakao Developers. Still, users were stuck, so I needed a fix fast, and I started by pulling every key hash I could get my hands on.

I logged them from inside the app, and I generated them from the app signing SHA-1 in the Google Play Console. Every single one was already registered.

Slightly panicked, I kept searching until I found a thread on Kakao DevTalk (Kakao's developer forum). The cause there was a key hash that hadn't been registered, and in some cases **deriving that key hash from the legacy SHA-1 certificate fingerprint fixed it**.

That rang a bell. I'd once lost my old jks file and had to get a new certificate fingerprint issued, which changed the SHA-1. So I took the legacy fingerprint that Play Console listed as "Not available", converted it to a key hash, registered it in Kakao Developers, and Kakao Login worked again.

![](images/02.webp){width=100 align=center}

Reading a painful review like that from a real user made me promise myself to test more carefully, on more setups, before every release.
