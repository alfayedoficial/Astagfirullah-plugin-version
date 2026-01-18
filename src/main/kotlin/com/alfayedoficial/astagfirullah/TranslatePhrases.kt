package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.cache.PraiseCacheService
import com.intellij.openapi.diagnostic.Logger

/**
 * Provides Islamic phrases in multiple languages.
 *
 * Data sources (in priority order):
 * 1. Cached API data (for Arabic and English)
 * 2. Static fallback data (for all languages)
 *
 * Supports: Arabic, English, Urdu, Farsi, Turkish, Indonesian (Bahasa), Bengali
 * API only provides Arabic and English; other languages use static data.
 */
object TranslatePhrases {

    private val logger = Logger.getInstance(TranslatePhrases::class.java)

    private val arabicPhrases = listOf(
        "اللهم صل وسلم على نبينا محمد",
        "سبحان الله",
        "الحمدلله",
        "لا إله إلا الله",
        "الله أكبر",
        "أستغفر الله",
        "سبحان الله وبحمده",
        "سبحان الله العظيم",
        "لا حول ولا قوة إلا بالله",
        "اللهم صل وسلم على نبينا محمد",
        "لا إله إلا أنت سبحانك إني كنت من الظالمين",
        "اللهم إني أعوذ بك من زوال نعمتك وتحول عافيتك وفجاءة نقمتك وجميع سخطك",
        "ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة وقنا عذاب النار",
        "اللهم أنت ربي لا إله إلا أنت خلقتني وأنا عبدك",
        "اللهم اجعلني مقيم الصلاة ومن ذريتي ربنا وتقبل دعاء",
        "اللهم إني أسألك العفو والعافية في الدنيا والآخرة",
        "اللهم اغفر لي ذنبي كله دقه وجله وأوله وآخره وعلانيته وسره",
        "اللهم اجعلني من التوابين واجعلني من المتطهرين",
        "ربنا ظلمنا أنفسنا وإن لم تغفر لنا وترحمنا لنكونن من الخاسرين",
        "رب اشرح لي صدري ويسر لي أمري واحلل عقدة من لساني يفقهوا قولي",
        "اللهم لا تجعل مصيبتنا في ديننا ولا تجعل الدنيا أكبر همنا ولا مبلغ علمنا",
        "اللهم إني أعوذ بك من شر ما عملت ومن شر ما لم أعمل",
        "ربنا تقبل منا إنك أنت السميع العليم",
        "ربنا هب لنا من أزواجنا وذرياتنا قرة أعين واجعلنا للمتقين إمامًا",
        "رب ارحمهما كما ربياني صغيرًا",
        "اللهم إني أعوذ بك من الهم والحزن والعجز والكسل والبخل والجبن وضلع الدين وغلبة الرجال",
        "اللهم آتنا في الدنيا حسنة وفي الآخرة حسنة وقنا عذاب النار",
        "اللهم اغفر لنا وارحمنا واهدنا وعافنا واعف عنا",
        "اللهم إني أسألك علماً نافعاً ورزقاً طيباً وعملاً متقبلاً",
        "اللهم إني أعوذ بك من قلب لا يخشع ومن دعاء لا يسمع ومن نفس لا تشبع ومن علم لا ينفع",
        "اللهم إني أسألك حبك وحب من يحبك وحب عمل يقربني إلى حبك"
    )

    private val englishPhrases = listOf(
        "O Allah, send blessings and peace upon our Prophet Muhammad",
        "Glory be to Allah",
        "Praise be to Allah",
        "There is no god but Allah",
        "Allah is the Greatest",
        "I seek forgiveness from Allah",
        "Glory be to Allah and with His praise",
        "Glory be to the Great Allah",
        "There is no power and no strength except with Allah",
        "O Allah, send blessings and peace upon our Prophet Muhammad",
        "There is no god but You, glory be to You, indeed I was among the wrongdoers",
        "O Allah, I seek refuge in You from the loss of Your blessings and the transformation of Your wellness and Your sudden wrath and all Your displeasure",
        "Our Lord, give us in this world that which is good and in the Hereafter that which is good and save us from the punishment of the Fire",
        "O Allah, You are my Lord, there is no god but You, You created me and I am Your servant",
        "O Allah, make me among those who establish prayer and among my descendants, our Lord, and accept my supplication",
        "O Allah, I ask You for forgiveness and well-being in this world and in the Hereafter",
        "O Allah, forgive me all my sins, the small and the great, the first and the last, the open and the secret",
        "O Allah, make me among the repentant and make me among the purified",
        "Our Lord, we have wronged ourselves, and if You do not forgive us and have mercy upon us, we will surely be among the losers",
        "My Lord, expand for me my chest and ease for me my task and untie the knot from my tongue that they may understand my speech",
        "O Allah, do not let our calamities be in our religion, and do not make this world our greatest concern or the limit of our knowledge",
        "O Allah, I seek refuge in You from the evil of what I have done and from the evil of what I have not done",
        "Our Lord, accept from us, indeed You are the Hearing, the Knowing",
        "Our Lord, grant us from among our wives and offspring comfort to our eyes and make us an example for the righteous",
        "My Lord, have mercy upon them as they brought me up [when I was] small",
        "O Allah, I seek refuge in You from worry and sorrow, weakness and laziness, miserliness and cowardice, the burden of debts and the oppression of men",
        "O Allah, give us in this world that which is good and in the Hereafter that which is good and save us from the punishment of the Fire",
        "O Allah, forgive us and have mercy on us and guide us and give us health and pardon us",
        "O Allah, I ask You for beneficial knowledge, good provision, and acceptable deeds",
        "O Allah, I seek refuge in You from a heart that does not fear, a supplication that is not heard, a soul that is not satisfied, and knowledge that does not benefit",
        "O Allah, I ask You for Your love, the love of those who love You, and the love of deeds that bring me closer to Your love"
    )

    private val urduPhrases = listOf(
        "اے اللہ، ہمارے نبی محمد پر رحمتیں اور سلام بھیجیں",
        "اللہ پاک ہے",
        "اللہ کا شکر ہے",
        "اللہ کے سوا کوئی معبود نہیں",
        "اللہ سب سے بڑا ہے",
        "میں اللہ سے معافی مانگتا ہوں",
        "اللہ پاک ہے اور اس کی تعریف کے ساتھ",
        "اللہ عظیم ہے",
        "اللہ کی مدد کے بغیر کوئی طاقت نہیں",
        "اے اللہ، ہمارے نبی محمد پر رحمتیں اور سلام بھیجیں",
        "اللہ کے سوا کوئی معبود نہیں، آپ پاک ہیں، میں بے شک ظالموں میں سے ہوں",
        "اے اللہ، میں تیری پناہ چاہتا ہوں تیری نعمت کے زوال سے، تیری صحت کے نقصان سے، تیری اچانک ناراضی سے، اور تیری تمام ناراضگی سے",
        "اے ہمارے رب، ہمیں دنیا میں بھلائی عطا کر اور آخرت میں بھلائی عطا کر اور ہمیں آگ کے عذاب سے بچا",
        "اے اللہ، تو میرا رب ہے، تیرے سوا کوئی معبود نہیں، تو نے مجھے پیدا کیا اور میں تیرا بندہ ہوں",
        "اے اللہ، مجھے نماز قائم کرنے والوں میں سے بنا دے اور میری اولاد میں سے بھی، ہمارے رب، اور میری دعا قبول فرما",
        "اے اللہ، میں تجھ سے دنیا اور آخرت میں معافی اور عافیت مانگتا ہوں",
        "اے اللہ، میرے تمام گناہ معاف فرما، چھوٹے اور بڑے، پہلے اور بعد کے، کھلے اور چھپے",
        "اے اللہ، مجھے توبہ کرنے والوں میں سے بنا دے اور مجھے پاک صاف لوگوں میں سے بنا دے",
        "اے ہمارے رب، ہم نے اپنے آپ پر ظلم کیا، اور اگر تو نے ہمیں معاف نہ کیا اور ہم پر رحم نہ کیا تو ہم ضرور نقصان اٹھانے والوں میں سے ہوں گے",
        "اے میرے رب، میرے سینے کو کھول دے اور میرے کام کو آسان کر دے اور میری زبان کی گرہ کو کھول دے تاکہ وہ میری بات کو سمجھ سکیں",
        "اے اللہ، ہماری مصیبتیں ہمارے دین میں نہ بننے دے، اور دنیا کو ہمارا سب سے بڑا مقصد یا علم کی حد نہ بنا",
        "اے اللہ، میں تجھ سے پناہ مانگتا ہوں اپنے کیے ہوئے شر سے اور جو میں نے نہیں کیا اس کے شر سے",
        "اے ہمارے رب، ہم سے قبول فرما، بے شک تو سننے والا اور جاننے والا ہے",
        "اے ہمارے رب، ہمیں ہماری بیویوں اور اولادوں میں سے آنکھوں کی ٹھنڈک عطا فرما اور ہمیں پرہیزگاروں کے لیے امام بنا",
        "اے میرے رب، ان پر رحم کر جیسا کہ انہوں نے مجھے چھوٹے ہوتے ہوئے پالا",
        "اے اللہ، میں تیری پناہ مانگتا ہوں فکر اور غم سے، کمزوری اور سستی سے، بخل اور بزدلی سے، قرضوں کے بوجھ سے اور لوگوں کے ظلم سے",
        "اے اللہ، ہمیں دنیا میں بھلائی عطا کر اور آخرت میں بھلائی عطا کر اور ہمیں آگ کے عذاب سے بچا",
        "اے اللہ، ہمیں معاف فرما، ہم پر رحم فرما، ہمیں ہدایت عطا فرما، ہمیں صحت عطا فرما، اور ہمیں معاف فرما",
        "اے اللہ، میں تجھ سے فائدہ مند علم، اچھا رزق، اور مقبول عمل کا سوال کرتا ہوں",
        "اے اللہ، میں تجھ سے پناہ مانگتا ہوں ایسے دل سے جو نہیں ڈرتا، ایسے دعا سے جو قبول نہیں ہوتی، ایسے نفس سے جو مطمئن نہیں ہوتا، اور ایسے علم سے جو فائدہ نہیں دیتا",
        "اے اللہ، میں تجھ سے تیری محبت، ان کی محبت جو تجھ سے محبت کرتے ہیں، اور ایسے عمل کی محبت کا سوال کرتا ہوں جو مجھے تیری محبت کے قریب لائے"
    )

    private val farsiPhrases = listOf(
        "اللهم صل وسلم على نبينا محمد",
        "سبحان الله",
        "الحمدلله",
        "لا إله إلا الله",
        "الله أكبر",
        "أستغفر الله",
        "سبحان الله وبحمده",
        "سبحان الله العظيم",
        "لا حول ولا قوة إلا بالله",
        "اللهم صل وسلم على نبينا محمد",
        "لا إله إلا أنت سبحانك إني كنت من الظالمين",
        "اللهم إني أعوذ بك من زوال نعمتك وتحول عافیتک وفجاءة نقمتک وجمیع سخطک",
        "ربنا آتنا فی الدنیا حسنة و فی الآخرة حسنة و قنا عذاب النار",
        "اللهم أنت ربی لا إله إلا أنت خلقتنی و أنا عبدک",
        "اللهم اجعلنی مقیم الصلاة و من ذریتی ربنا و تقبل دعاء",
        "اللهم إنی أسألک العفو و العافیة فی الدنیا و الآخرة",
        "اللهم اغفر لی ذنبی کله دقه و جله و أوله و آخره و علانیته و سره",
        "اللهم اجعلنی من التوابین و اجعلنی من المتطهرین",
        "ربنا ظلمنا أنفسنا و إن لم تغفر لنا و ترحمنا لنکونن من الخاسرین",
        "رب اشرح لی صدری و یسر لی أمری و احلل عقدة من لسانی یفقهوا قولی",
        "اللهم لا تجعل مصیبتنا فی دیننا و لا تجعل الدنیا أکبر همنا و لا مبلغ علمنا",
        "اللهم إنی أعوذ بک من شر ما عملت و من شر ما لم أعمل",
        "ربنا تقبل منا إنک أنت السمیع العلیم",
        "ربنا هب لنا من أزواجنا و ذریاتنا قرة أعین و اجعلنا للمتقین إمامًا",
        "رب ارحمهما کما ربیانی صغیرًا",
        "اللهم إنی أعوذ بک من الهم و الحزن و العجز و الکسل و البخل و الجبن و ضلع الدین و غلبة الرجال",
        "اللهم آتنا فی الدنیا حسنة و فی الآخرة حسنة و قنا عذاب النار",
        "اللهم اغفر لنا و ارحمنا و اهدنا و عافنا و اعف عنا",
        "اللهم إنی أسألک علمًا نافعًا و رزقًا طیبًا و عملًا متقبلًا",
        "اللهم إنی أعوذ بک من قلب لا یخشع و من دعاء لا یسمع و من نفس لا تشبع و من علم لا ینفع",
        "اللهم إنی أسألک حبک و حب من یحبک و حب عمل یقربنی إلى حبک"
    )

    // Turkish phrases
    private val turkishPhrases = listOf(
        "Allah'ım, Peygamberimiz Muhammed'e salat ve selam eyle",
        "Sübhanallah",
        "Elhamdülillah",
        "La ilahe illallah",
        "Allahu Ekber",
        "Estağfirullah",
        "Sübhanallahi ve bihamdihi",
        "Sübhanallahil azim",
        "La havle vela kuvvete illa billah",
        "Allah'ım, Peygamberimiz Muhammed'e salat ve selam eyle",
        "Senden başka ilah yoktur, Seni tenzih ederim, ben zalimlerden oldum",
        "Allah'ım, nimetinin yok olmasından, afiyetinin değişmesinden, ansızın gazabına uğramaktan ve bütün gazabından Sana sığınırım",
        "Rabbimiz, bize dünyada iyilik ver, ahirette de iyilik ver ve bizi cehennem azabından koru",
        "Allah'ım, Sen benim Rabbimsin, Senden başka ilah yoktur, beni Sen yarattın ve ben Senin kulunum",
        "Allah'ım, beni ve soyumu namaz kılanlardan eyle, Rabbimiz, duamı kabul et",
        "Allah'ım, dünya ve ahirette Senden af ve afiyet dilerim",
        "Allah'ım, küçük büyük, ilk son, açık gizli tüm günahlarımı bağışla",
        "Allah'ım, beni tövbe edenlerden ve temizlenenlerden eyle",
        "Rabbimiz, biz kendimize zulmettik, eğer bizi bağışlamaz ve bize merhamet etmezsen, hüsrana uğrayanlardan oluruz",
        "Rabbim, göğsümü aç, işimi kolaylaştır ve dilimden düğümü çöz ki sözümü anlasınlar",
        "Allah'ım, musibetimizi dinimizde kılma, dünyayı en büyük kaygımız ve ilmimizin sınırı yapma",
        "Allah'ım, yaptığım ve yapmadığım şeylerin şerrinden Sana sığınırım",
        "Rabbimiz, bizden kabul et, şüphesiz Sen işitensin, bilensin",
        "Rabbimiz, eşlerimizden ve çocuklarımızdan bize göz aydınlığı ver ve bizi takva sahiplerine önder kıl",
        "Rabbim, onlara merhamet et, nasıl ki onlar beni küçükken büyüttüler",
        "Allah'ım, kaygı ve üzüntüden, acizlik ve tembellikten, cimrilik ve korkaklıktan, borç yükünden ve insanların zorbalığından Sana sığınırım",
        "Allah'ım, bize dünyada iyilik ver, ahirette de iyilik ver ve bizi cehennem azabından koru",
        "Allah'ım, bizi bağışla, bize merhamet et, bizi doğru yola ilet, bize afiyet ver ve bizi affet",
        "Allah'ım, Senden faydalı ilim, helal rızık ve makbul amel dilerim",
        "Allah'ım, korkmayan kalpten, işitilmeyen duadan, doymayan nefisten ve fayda vermeyen ilimden Sana sığınırım",
        "Allah'ım, Senden sevgini, Seni sevenlerin sevgisini ve beni sevgine yaklaştıracak amelin sevgisini dilerim"
    )

    // Indonesian (Bahasa) phrases
    private val indonesianPhrases = listOf(
        "Ya Allah, limpahkanlah shalawat dan salam kepada Nabi Muhammad",
        "Subhanallah",
        "Alhamdulillah",
        "La ilaha illallah",
        "Allahu Akbar",
        "Astaghfirullah",
        "Subhanallahi wa bihamdihi",
        "Subhanallahil 'azhim",
        "La haula wala quwwata illa billah",
        "Ya Allah, limpahkanlah shalawat dan salam kepada Nabi Muhammad",
        "Tidak ada Tuhan selain Engkau, Mahasuci Engkau, sesungguhnya aku termasuk orang-orang yang zalim",
        "Ya Allah, aku berlindung kepada-Mu dari hilangnya nikmat-Mu, berubahnya kesehatan-Mu, datangnya murka-Mu secara tiba-tiba, dan semua kemurkaan-Mu",
        "Ya Tuhan kami, berilah kami kebaikan di dunia dan kebaikan di akhirat, dan lindungilah kami dari siksa neraka",
        "Ya Allah, Engkau adalah Tuhanku, tidak ada Tuhan selain Engkau, Engkau menciptakanku dan aku adalah hamba-Mu",
        "Ya Allah, jadikanlah aku dan keturunanku orang yang mendirikan shalat, Ya Tuhan kami, kabulkanlah doaku",
        "Ya Allah, aku memohon kepada-Mu ampunan dan kesehatan di dunia dan akhirat",
        "Ya Allah, ampunilah semua dosaku, yang kecil dan besar, yang pertama dan terakhir, yang terang-terangan dan yang tersembunyi",
        "Ya Allah, jadikanlah aku termasuk orang-orang yang bertaubat dan orang-orang yang menyucikan diri",
        "Ya Tuhan kami, kami telah menzalimi diri kami sendiri, dan jika Engkau tidak mengampuni kami dan merahmati kami, niscaya kami termasuk orang-orang yang merugi",
        "Ya Tuhanku, lapangkanlah dadaku, mudahkanlah urusanku, dan lepaskanlah kekakuan dari lidahku agar mereka memahami perkataanku",
        "Ya Allah, janganlah Engkau jadikan musibah kami dalam agama kami, dan janganlah Engkau jadikan dunia sebagai tujuan terbesar kami atau batas ilmu kami",
        "Ya Allah, aku berlindung kepada-Mu dari kejahatan apa yang telah aku lakukan dan dari kejahatan apa yang tidak aku lakukan",
        "Ya Tuhan kami, terimalah dari kami, sesungguhnya Engkau Maha Mendengar lagi Maha Mengetahui",
        "Ya Tuhan kami, anugerahkanlah kepada kami pasangan dan keturunan yang menyejukkan hati kami, dan jadikanlah kami pemimpin bagi orang-orang yang bertakwa",
        "Ya Tuhanku, sayangilah keduanya sebagaimana mereka menyayangiku waktu kecil",
        "Ya Allah, aku berlindung kepada-Mu dari kegelisahan dan kesedihan, kelemahan dan kemalasan, kebakhilan dan kepengecutan, lilitan hutang dan kekerasan manusia",
        "Ya Allah, berilah kami kebaikan di dunia dan kebaikan di akhirat, dan lindungilah kami dari siksa neraka",
        "Ya Allah, ampunilah kami, rahmatilah kami, tunjukilah kami, berilah kami kesehatan, dan maafkanlah kami",
        "Ya Allah, aku memohon kepada-Mu ilmu yang bermanfaat, rezeki yang baik, dan amal yang diterima",
        "Ya Allah, aku berlindung kepada-Mu dari hati yang tidak khusyuk, doa yang tidak didengar, jiwa yang tidak puas, dan ilmu yang tidak bermanfaat",
        "Ya Allah, aku memohon kepada-Mu cinta-Mu, cinta orang yang mencintai-Mu, dan cinta amal yang mendekatkanku kepada cinta-Mu"
    )

    // Bengali phrases
    private val bengaliPhrases = listOf(
        "হে আল্লাহ, আমাদের নবী মুহাম্মদের উপর দরূদ ও সালাম বর্ষণ করুন",
        "সুবহানাল্লাহ",
        "আলহামদুলিল্লাহ",
        "লা ইলাহা ইল্লাল্লাহ",
        "আল্লাহু আকবার",
        "আস্তাগফিরুল্লাহ",
        "সুবহানাল্লাহি ওয়া বিহামদিহি",
        "সুবহানাল্লাহিল আজীম",
        "লা হাওলা ওয়ালা কুওয়াতা ইল্লা বিল্লাহ",
        "হে আল্লাহ, আমাদের নবী মুহাম্মদের উপর দরূদ ও সালাম বর্ষণ করুন",
        "আপনি ছাড়া কোন ইলাহ নেই, আপনি পবিত্র, নিশ্চয়ই আমি জালিমদের অন্তর্ভুক্ত",
        "হে আল্লাহ, আমি আপনার নিকট আশ্রয় চাই আপনার নিয়ামত বিলুপ্ত হওয়া থেকে, আপনার সুস্থতা পরিবর্তন হওয়া থেকে, আপনার আকস্মিক শাস্তি থেকে এবং আপনার সমস্ত অসন্তুষ্টি থেকে",
        "হে আমাদের রব, আমাদের দুনিয়াতে কল্যাণ দিন এবং আখিরাতেও কল্যাণ দিন এবং আমাদেরকে জাহান্নামের আগুন থেকে রক্ষা করুন",
        "হে আল্লাহ, আপনি আমার রব, আপনি ছাড়া কোন ইলাহ নেই, আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা",
        "হে আল্লাহ, আমাকে এবং আমার বংশধরদের নামাজ কায়েমকারী বানান, হে আমাদের রব, আমার দোয়া কবুল করুন",
        "হে আল্লাহ, আমি আপনার কাছে দুনিয়া ও আখিরাতে ক্ষমা ও সুস্থতা চাই",
        "হে আল্লাহ, আমার সমস্ত গুনাহ ক্ষমা করুন, ছোট ও বড়, প্রথম ও শেষ, প্রকাশ্য ও গোপন",
        "হে আল্লাহ, আমাকে তওবাকারীদের এবং পবিত্রতা অর্জনকারীদের অন্তর্ভুক্ত করুন",
        "হে আমাদের রব, আমরা নিজেদের উপর জুলুম করেছি, যদি আপনি আমাদের ক্ষমা না করেন এবং আমাদের প্রতি রহম না করেন, তাহলে আমরা অবশ্যই ক্ষতিগ্রস্তদের অন্তর্ভুক্ত হব",
        "হে আমার রব, আমার বুক খুলে দিন, আমার কাজ সহজ করে দিন এবং আমার জিহ্বা থেকে জড়তা দূর করুন যাতে তারা আমার কথা বুঝতে পারে",
        "হে আল্লাহ, আমাদের বিপদ আমাদের দ্বীনে দিবেন না, দুনিয়াকে আমাদের সবচেয়ে বড় লক্ষ্য বা আমাদের জ্ঞানের সীমা বানাবেন না",
        "হে আল্লাহ, আমি আপনার কাছে আশ্রয় চাই আমি যা করেছি তার অনিষ্ট থেকে এবং যা আমি করিনি তার অনিষ্ট থেকে",
        "হে আমাদের রব, আমাদের থেকে কবুল করুন, নিশ্চয়ই আপনি সর্বশ্রোতা, সর্বজ্ঞ",
        "হে আমাদের রব, আমাদের স্ত্রী ও সন্তানদের থেকে আমাদের চোখের শীতলতা দান করুন এবং আমাদের মুত্তাকীদের জন্য ইমাম বানান",
        "হে আমার রব, তাদের প্রতি রহম করুন যেমন তারা আমাকে ছোটবেলায় লালন-পালন করেছেন",
        "হে আল্লাহ, আমি আপনার কাছে আশ্রয় চাই দুশ্চিন্তা ও দুঃখ থেকে, দুর্বলতা ও অলসতা থেকে, কৃপণতা ও ভীরুতা থেকে, ঋণের বোঝা ও মানুষের অত্যাচার থেকে",
        "হে আল্লাহ, আমাদের দুনিয়াতে কল্যাণ দিন এবং আখিরাতেও কল্যাণ দিন এবং আমাদের জাহান্নামের আগুন থেকে রক্ষা করুন",
        "হে আল্লাহ, আমাদের ক্ষমা করুন, আমাদের প্রতি রহম করুন, আমাদের হেদায়েত দিন, আমাদের সুস্থতা দিন এবং আমাদের মাফ করুন",
        "হে আল্লাহ, আমি আপনার কাছে উপকারী ইলম, উত্তম রিযিক এবং কবুল হওয়া আমল চাই",
        "হে আল্লাহ, আমি আপনার কাছে আশ্রয় চাই এমন অন্তর থেকে যা ভয় করে না, এমন দোয়া থেকে যা শোনা হয় না, এমন আত্মা থেকে যা তৃপ্ত হয় না, এবং এমন ইলম থেকে যা উপকার দেয় না",
        "হে আল্লাহ, আমি আপনার কাছে আপনার ভালোবাসা চাই, যারা আপনাকে ভালোবাসে তাদের ভালোবাসা চাই, এবং এমন আমলের ভালোবাসা চাই যা আমাকে আপনার ভালোবাসার কাছে নিয়ে যাবে"
    )

    // Static fallback phrases map (used when API data not available)
    private val staticPhrasesMap = mapOf(
        "العربية" to arabicPhrases,
        "English" to englishPhrases,
        "أردو" to urduPhrases,
        "فارسى" to farsiPhrases,
        "Türkçe" to turkishPhrases,
        "Bahasa" to indonesianPhrases,
        "বাংলা" to bengaliPhrases
    )

    private val titlesMap = mapOf(
        "العربية" to "اذكر الله",
        "English" to "Remember Allah",
        "أردو" to "اللہ کی یاد",
        "فارسى" to "یاد خدا",
        "Türkçe" to "Allah'ı An",
        "Bahasa" to "Ingat Allah",
        "বাংলা" to "আল্লাহকে স্মরণ করুন"
    )

    // Languages supported by API
    private val apiSupportedLanguages = setOf("العربية", "English")

    /**
     * Returns a random selection of phrases in the user's preferred language.
     * Uses cached API data for Arabic/English, static data for other languages.
     *
     * @return List of [Constants.PHRASES_PER_DISPLAY] random phrases
     */
    fun selectedTranslatePhrases(): List<String> {
        val language = AstagfirullahSettings.getInstance().language
        logger.debug("Selected language: $language")
        val phrases = getPhrasesForLanguage(language)
        logger.debug("Total phrases available: ${phrases.size}")
        val selected = phrases.shuffled().take(Constants.PHRASES_PER_DISPLAY)
        logger.debug("Selected ${selected.size} phrases for display")
        return selected
    }

    /**
     * Gets phrases for a specific language.
     * Priority: Cached API data > Static fallback data
     *
     * @param language The language code
     * @return List of phrases
     */
    private fun getPhrasesForLanguage(language: String): List<String> {
        logger.debug("Getting phrases for language: $language")

        // For API-supported languages, try cache first
        if (language in apiSupportedLanguages) {
            val cachedPhrases = getCachedPhrasesForLanguage(language)
            if (cachedPhrases.isNotEmpty()) {
                logger.debug("Using ${cachedPhrases.size} cached API phrases for $language")
                return cachedPhrases
            }
        }

        // Fallback to static data
        val staticPhrases = staticPhrasesMap[language] ?: staticPhrasesMap[Constants.DEFAULT_LANGUAGE]!!
        logger.debug("Using ${staticPhrases.size} static phrases for $language")
        return staticPhrases
    }

    /**
     * Gets cached phrases for Arabic or English.
     */
    private fun getCachedPhrasesForLanguage(language: String): List<String> {
        return try {
            val cacheService = PraiseCacheService.getInstance()
            when (language) {
                "العربية" -> cacheService.getArabicPhrases()
                "English" -> cacheService.getEnglishPhrases()
                else -> emptyList()
            }
        } catch (e: Exception) {
            logger.warn("Failed to get cached phrases: ${e.message}")
            emptyList()
        }
    }

    /**
     * Returns the localized title for the phrase display.
     * @return Title in the user's preferred language
     */
    fun selectTranslateTitle(): String {
        val language = AstagfirullahSettings.getInstance().language
        return titlesMap[language] ?: titlesMap[Constants.DEFAULT_LANGUAGE]!!
    }

    /**
     * Returns all available phrases for a specific language.
     * @param language The language code
     * @return All phrases in that language
     */
    fun getAllPhrases(language: String): List<String> {
        return getPhrasesForLanguage(language)
    }

    /**
     * Returns the total number of phrases available for current language.
     */
    val phrasesCount: Int
        get() {
            val language = AstagfirullahSettings.getInstance().language
            return getPhrasesForLanguage(language).size
        }

    /**
     * Checks if API data is available for current language.
     */
    fun hasApiData(): Boolean {
        return try {
            val language = AstagfirullahSettings.getInstance().language
            language in apiSupportedLanguages && PraiseCacheService.getInstance().hasCachedData()
        } catch (e: Exception) {
            false
        }
    }
}
