package com.alfayedoficial.astagfirullah

object TranslatePhrases{

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

    fun selectedTranslatePhrases(): List<String> {
        val savedLanguage = PropertiesManager.getValue("preferredLanguage", "العربية")

        val phrases = when(savedLanguage){
            "العربية" -> TranslatePhrases.arabicPhrases
            "English" -> TranslatePhrases.englishPhrases
            "أوردو" -> TranslatePhrases.urduPhrases
            "فارسى" -> TranslatePhrases.farsiPhrases
            else -> TranslatePhrases.arabicPhrases
        }

        // take random 10 phrases from the list
        return phrases.shuffled().take(10)
    }

    fun selectTranslateTitle(): String {
        val savedLanguage = PropertiesManager.getValue("preferredLanguage", "العربية")
        return when(savedLanguage){
            "العربية" -> "اذكر الله"
            "English" -> "Remember Allah"
            "أوردو" -> "اللہ کی یاد"
            "فارسى" -> "یاد خدا"
            else -> "اذكر الله"
        }
    }
}
