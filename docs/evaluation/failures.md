# Failures

October 15, 2026: 29 failures from 50 real MiniLM requests using the extractive fallback.
No Groq/Llama answers were evaluated. Original questions and scoring rules were unchanged.
See [summary.md](summary.md) and [run-context.json](run-context.json) for configuration and provenance.

## q02: Which astronaut stayed in lunar orbit aboard the command module while the others landed on the Moon during Apollo 11?

- Cause: answer wrong and citation wrong
- Expected keywords: ['Collins']
- Gold articles: ['Apollo 11']
- Retrieved: ['Apollo 13', 'Apollo 11', 'Apollo 13', 'Apollo 11', 'Apollo 11']
- Cited: ['Apollo 13']
- Answer: The crew, supported by backup systems on the Apollo Lunar Module, instead looped around the Moon in a circumlunar trajectory and returned safely to Earth on April 17. The mission was commanded by Jim Lovell, with Jack Swigert as command module (CM) pilot and Fred Haise as Lunar Module (LM) pilot. [1]

## q03: On what date did Apollo 11 land on the Moon?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['July 20|20 July']
- Gold articles: ['Apollo 11']
- Retrieved: ['Apollo 11', 'Apollo 11', 'Apollo 13', 'Apollo 11', 'Apollo 11']
- Cited: ['Apollo 11']
- Answer: On July 19 at 17:21:50 UTC, Apollo 11 passed behind the Moon and fired its service propulsion engine to enter lunar orbit. In the thirty orbits that followed, the crew saw passing views of their landing site in the southern Sea of Tranquility about 12 miles (19 km) southwest of the crater Sabine D. [1]

## q04: What was the name of the Apollo 11 lunar module?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Eagle']
- Gold articles: ['Apollo 11']
- Retrieved: ['Apollo 11', 'Apollo 11', 'Apollo 13', 'Apollo program', 'Apollo 13']
- Cited: ['Apollo 11']
- Answer: Launched atop a Saturn V rocket from Kennedy Space Center in Florida on July 16 at 13:32 UTC, the Apollo spacecraft consisted of three parts: the command module (CM), which housed the three astronauts and was the only part to return to Earth; the service module (SM), which provided propulsion, electrical power, oxygen, and water to the command module; and the Lunar Module (LM), which had two stages—a descent stage with a large engine and fuel tanks for landing on [1]

## q05: What was the name of the Apollo 11 command module?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Columbia']
- Gold articles: ['Apollo 11']
- Retrieved: ['Apollo program', 'Apollo 11', 'Apollo 11', 'Apollo 11', 'Apollo 11']
- Cited: ['Apollo 11']
- Answer: In July 1962, NASA head James Webb announced that lunar orbit rendezvous would be used and that the Apollo spacecraft would have three major parts: a command module (CM) with a cabin for the three astronauts, and the only part that returned to Earth; a service module (SM), which supported the command module with propulsion, electrical power, oxygen, and water; and a lunar module (LM) that had two stages—a descent stage for landing on the Moon, and an ascent stage [2]

## q07: What failure forced Apollo 13 to abort its Moon landing?

- Cause: answer right, citation missing or from the wrong article
- Expected keywords: ['oxygen']
- Gold articles: ['Apollo 13']
- Retrieved: ['Apollo program', 'Apollo 11', 'Apollo 13', 'Apollo 13', 'Apollo 11']
- Cited: ['Apollo program']
- Answer: Five of the remaining six missions achieved landings; but the Apollo 13 landing had to be aborted after an oxygen tank exploded en route to the Moon, crippling the CSM. [1]

## q09: Who replaced Ken Mattingly as command module pilot on Apollo 13?

- Cause: answer wrong and citation wrong
- Expected keywords: ['Swigert']
- Gold articles: ['Apollo 13']
- Retrieved: ['Apollo 11', 'Apollo 13', 'Apollo 11', 'Apollo 13', 'Apollo 13']
- Cited: ['Apollo 11']
- Answer: Ken Mattingly was moved from the support crew into parallel training with Anders as backup CMP in case Apollo 11 was delayed past its intended July launch date, at which point Anders would be unavailable. Under the normal Apollo crew rotation, Lovell, Mattingly, and Haise would have been assigned to Apollo 14. [1]

## q10: What was the name of the Apollo 13 lunar module that the crew used as a lifeboat?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Aquarius']
- Gold articles: ['Apollo 13']
- Retrieved: ['Apollo 13', 'Apollo 13', 'Apollo 13', 'Apollo 11', 'Apollo program']
- Cited: ['Apollo 13']
- Answer: The crew, supported by backup systems on the Apollo Lunar Module, instead looped around the Moon in a circumlunar trajectory and returned safely to Earth on April 17. The mission was commanded by Jim Lovell, with Jack Swigert as command module (CM) pilot and Fred Haise as Lunar Module (LM) pilot. [2]

## q11: Which ship recovered the Apollo 13 crew after splashdown?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Iwo Jima']
- Gold articles: ['Apollo 13']
- Retrieved: ['Apollo 13', 'Apollo 13', 'Apollo 13', 'Apollo 11', 'Apollo 11']
- Cited: ['Apollo 13']
- Answer: The change would get Apollo 13 back to Earth in about four days' time – though with splashdown in the Indian Ocean, where NASA had few recovery forces. Jerry Bostick and other Flight Dynamics Officers (FIDOs) were anxious both to shorten the travel time and to move splashdown to the Pacific Ocean, where the main recovery forces were located. [3]

## q12: Which engineer led the team that developed the Saturn V rocket?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['von Braun']
- Gold articles: ['Saturn V']
- Retrieved: ['Saturn V', 'Saturn V', 'Saturn V', 'Saturn V', 'Saturn V']
- Cited: ['Saturn V']
- Answer: Unrealized follow-on concepts included the Saturn II, Saturn MLV, and Saturn V ELV vehicles, which could include UA120 solid rocket boosters, the Saturn C-5N using a NERVA nuclear thermal rocket third stage, and the Saturn-Shuttle. [2]

## q14: What propellants did the first stage of the Saturn V burn?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['RP-1|kerosene', 'oxygen']
- Gold articles: ['Saturn V']
- Retrieved: ['Saturn V', 'Saturn V', 'Saturn V', 'Saturn V', 'Saturn V']
- Cited: ['Saturn V']
- Answer: The second stage accelerated the Saturn V through the upper atmosphere with 4,400 kN (1,000,000 lbf) of thrust in a vacuum. The S-II had a burn time of 395 seconds. [3]

## q17: Who was the first American in space?

- Cause: answer right, citation missing or from the wrong article
- Expected keywords: ['Shepard']
- Gold articles: ['Project Mercury']
- Retrieved: ['Apollo 11', 'Project Mercury', 'Project Mercury', 'Project Mercury', 'Apollo 13']
- Cited: ['Apollo 11']
- Answer: Nearly a month later, on May 5, 1961, Alan Shepard became the first American in space; his 15-minute flight was suborbital, not a full orbit. Because the Soviet Union had launch vehicles with higher lift capacity, President John F. [1]

## q19: What was the group of astronauts selected for Project Mercury called?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Mercury Seven|Mercury 7']
- Gold articles: ['Project Mercury']
- Retrieved: ['Project Mercury', 'Project Mercury', 'Project Mercury', 'Project Mercury', 'Project Mercury']
- Cited: ['Project Mercury']
- Answer: Prior to Project Mercury, there was no protocol for selecting astronauts, so NASA would set a far-reaching precedent with both their selection process and initial choices for astronauts. At the end of 1958, various ideas for the selection pool were discussed privately within the national government and the civilian space program, and also among the public at large. [1]

## q22: How many crewed flights did Project Gemini have?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['10|ten']
- Gold articles: ['Project Gemini']
- Retrieved: ['Project Gemini', 'Project Gemini', 'Project Gemini', 'Project Gemini', 'Project Gemini']
- Cited: ['Project Gemini']
- Answer: Project Gemini (IPA: ) was the second United States human spaceflight program to fly. It was conducted after the first American crewed space program, Project Mercury, while the Apollo program was still in early development. [1]

## q23: How many astronauts flew on each Gemini spacecraft?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['two|2']
- Gold articles: ['Project Gemini']
- Retrieved: ['Project Gemini', 'Apollo program', 'Project Gemini', 'Project Gemini', 'Project Mercury']
- Cited: ['Project Gemini']
- Answer: Sixteen astronauts flew on 10 crewed Gemini missions: In late 1963, Slayton selected Shepard and Stafford for Gemini 3, McDivitt and White for Gemini 4, and Schirra and Young for Gemini 5 (which was to be the first Agena rendezvous mission). [1]

## q25: Which country launched Sputnik 1?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Soviet']
- Gold articles: ['Sputnik 1']
- Retrieved: ['Sputnik 1', 'Sputnik 1', 'Sputnik 1', 'Sputnik 1', 'Sputnik 1']
- Cited: ['Sputnik 1']
- Answer: Tracking and studying Sputnik 1 from Earth provided scientists with valuable information. [1]

## q26: How much did Sputnik 1 weigh?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['83.6']
- Gold articles: ['Sputnik 1']
- Retrieved: ['Sputnik 1', 'Sputnik 1', 'Sputnik 1', 'Sputnik 1', 'Sputnik 1']
- Cited: ['Sputnik 1']
- Answer: On 22 September a modified R-7 rocket, named Sputnik and indexed as 8K71PS, arrived at the proving ground and preparations for the launch of PS-1 began. Compared to the military R-7 test vehicles, the mass of 8K71PS was reduced from 280 to 272 tonnes (617,000 to 600,000 lb), its length with PS-1 was 29.167 metres (95 ft 8.3 in) and the thrust at liftoff was 3.90 MN (880,000 lbf). [1]

## q27: Which rocket launched Sputnik 1?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['R-7']
- Gold articles: ['Sputnik 1']
- Retrieved: ['Sputnik 1', 'Sputnik 1', 'Sputnik 1', 'Sputnik 1', 'Sputnik 1']
- Cited: ['Sputnik 1']
- Answer: The Sputnik rocket was launched on 4 October 1957 at 19:28:34 UTC (5 October at the launch site) from Site No.1 at NIIP-5. Telemetry indicated that the strap-ons separated 116 seconds into the flight and the core stage engine shut down 295.4 seconds into the flight. [2]

## q28: Who was the first human to travel into space?

- Cause: retrieval miss
- Expected keywords: ['Gagarin']
- Gold articles: ['Vostok 1']
- Retrieved: ['Apollo 11', 'Project Mercury', 'Project Mercury', 'Project Mercury', 'Project Mercury']
- Cited: ['Project Mercury']
- Answer: The Soviet Union put the first human, cosmonaut Yuri Gagarin, into a single orbit aboard Vostok 1 on April 12, 1961. Shortly after this, on May 5, the US launched its first astronaut, Alan Shepard, on a suborbital flight. [3]

## q30: How long did the Vostok 1 flight last?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['108']
- Gold articles: ['Vostok 1']
- Retrieved: ['Vostok 1', 'Vostok 1', 'Vostok 1', 'Vostok 1', 'Project Mercury']
- Cited: ['Vostok 1']
- Answer: Siddiqi wrote that Vostok 1 will undoubtedly remain one of the major milestones in not only the history of space exploration, but also the history of the human race itself. The fact that this accomplishment was successfully carried out by the Soviet Union, a country completely devastated by war just sixteen years prior, makes the achievement even more impressive. [1]

## q31: When was Skylab launched?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['May 14|14 May']
- Gold articles: ['Skylab']
- Retrieved: ['Skylab', 'Skylab', 'Skylab', 'Skylab', 'Skylab']
- Cited: ['Skylab']
- Answer: Skylab 1 was the last uncrewed launch from LC-39A until February 19, 2017, when SpaceX CRS-10 was launched from there. [1]

## q32: Which launch vehicle put Skylab into orbit?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Saturn V']
- Gold articles: ['Skylab', 'Saturn V']
- Retrieved: ['Skylab', 'Skylab', 'Skylab', 'Skylab', 'Skylab']
- Cited: ['Skylab']
- Answer: Skylab 5 would have been a short 20-day mission in April 1974 to conduct more scientific experiments and use the Apollo's Service Propulsion System engine to boost Skylab into a higher orbit, supporting later station use by the Space Shuttle. Vance Brand (commander), William B. [2]

## q38: Which module was the first to be launched for the International Space Station?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Zarya']
- Gold articles: ['International Space Station']
- Retrieved: ['International Space Station', 'International Space Station', 'International Space Station', 'International Space Station', 'Skylab']
- Cited: ['International Space Station']
- Answer: The first ISS module was launched in 1998, with major components delivered by Proton, Soyuz and Space Shuttle launch vehicles. Long-term occupancy began with the arrival of the Expedition 1 crew on 2 November 2000. [3]

## q41: Since what date has the International Space Station been continuously occupied?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['November', '2000']
- Gold articles: ['International Space Station']
- Retrieved: ['International Space Station', 'International Space Station', 'International Space Station', 'International Space Station', 'International Space Station']
- Cited: ['International Space Station']
- Answer: Since then, the ISS has remained continuously inhabited for 25 years and 346 days, the longest continuous human presence in space. [3]

## q42: On what date was the Hubble Space Telescope launched?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['April 24|24 April']
- Gold articles: ['Hubble Space Telescope']
- Retrieved: ['Hubble Space Telescope', 'Hubble Space Telescope', 'Hubble Space Telescope', 'Hubble Space Telescope', 'Hubble Space Telescope']
- Cited: ['Hubble Space Telescope']
- Answer: By January 1986, the planned launch date for Hubble on 18 October as part of STS-61-J looked feasible, but the Challenger disaster brought the U.S. space program to a halt, grounded the Shuttle fleet, and forced the launch to be postponed for several years. [1]

## q45: What flaw was found in Hubble's primary mirror after launch?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['spherical aberration']
- Gold articles: ['Hubble Space Telescope']
- Retrieved: ['Hubble Space Telescope', 'Hubble Space Telescope', 'Hubble Space Telescope', 'Hubble Space Telescope', 'Hubble Space Telescope']
- Cited: ['Hubble Space Telescope']
- Answer: The first Hubble servicing mission was scheduled for 1993 before the mirror problem was discovered. It assumed greater importance, as the astronauts would need to do extensive work to install corrective optics; failure would have resulted in either abandoning Hubble or accepting its permanent disability. [2]

## q46: Which Space Shuttle orbiter carried out the first Hubble servicing mission?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Endeavour']
- Gold articles: ['Hubble Space Telescope']
- Retrieved: ['Hubble Space Telescope', 'Hubble Space Telescope', 'Hubble Space Telescope', 'Space Shuttle program', 'Hubble Space Telescope']
- Cited: ['Hubble Space Telescope']
- Answer: Five servicing missions (SM 1, 2, 3A, 3B, and 4) were flown by NASA Space Shuttles, the first in December 1993 and the last in May 2009. [1]

## q47: In what month and year was Voyager 2 launched?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['August', '1977']
- Gold articles: ['Voyager program']
- Retrieved: ['Voyager program', 'Voyager program', 'Voyager program', 'Voyager program', 'Voyager program']
- Cited: ['Voyager program']
- Answer: On 5 November 2019, data from Voyager 2 indicated that it also had entered interstellar space. On 4 November 2019, scientists reported that on 5 November 2018, the Voyager 2 probe had officially reached the interstellar medium (ISM), a region of outer space beyond the influence of the solar wind, as did Voyager 1 in 2012. [1]

## q48: Which planets did Voyager 2 visit?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Jupiter', 'Saturn', 'Uranus', 'Neptune']
- Gold articles: ['Voyager program']
- Retrieved: ['Voyager program', 'Voyager program', 'Voyager program', 'Voyager program', 'Voyager program']
- Cited: ['Voyager program']
- Answer: After Voyager 1 successfully completed its flyby of Saturn and its moon Titan, it was decided that Voyager 2 would continue on its pre-planned trajectory to fly by Uranus and Neptune. After the planetary flybys were complete, decisions were made to keep the probes in operation to explore interstellar space and the outer regions of the Solar System. [1]

## q50: What disc was attached to each Voyager spacecraft to carry sounds and images of Earth?

- Cause: answer wrong or incomplete, citation right
- Expected keywords: ['Golden Record']
- Gold articles: ['Voyager program']
- Retrieved: ['Voyager program', 'Voyager program', 'Voyager program', 'Voyager program', 'Voyager program']
- Cited: ['Voyager program']
- Answer: Both spacecraft carry a 12-inch (30 cm) golden phonograph record that contains pictures and sounds of Earth, symbolic directions on the cover for playing the record, and data detailing the location of Earth. The record is intended as a combination time capsule and an interstellar message to any civilization, alien or far-future human, that may recover either of the Voyagers. [1]
