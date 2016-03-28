
import java.io.BufferedWriter;
import java.io.FileWriter;


public class Main {
    public static void main(String[] args) throws Exception{
        String filename = "result.txt";

        int eventId = 14000;
        int count = 50;
        int userIdFirst = 1500;

        int imageLimit = 30;
        int summaryLimit = 30;
        int newsLimit = 30;
        int tweetLimit = 30;

        FileWriter fileWriter = null;
        BufferedWriter out = null;
        try {
            fileWriter = new FileWriter(filename, true);
            out = new BufferedWriter(fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (int userId = userIdFirst; userId < (userIdFirst + count); userId++) {
            out.write("INSERT INTO `users` (`id`, `gcmToken`) VALUES " +
                    "("+ userId +", 'eRfKaQBQ3Yg:APA91bHrLZelPAbtG__OZTGbMx8ea8htF0e7e8a8QcBatDdEU4mkroBuKku45r6aclC03nvV7avqJJ4whXYxouV1ULZ3SyV0qcHRG3SC2F6QndmYeKUH5JPNl2aFvmN5hPXNnBopmNkc');");
            out.newLine();

            out.newLine();out.write("INSERT INTO `events` (`id`, `timestamp`, `heading`, `mainImageUrl`) VALUES ");
            out.newLine();out.write("(" + eventId + ", '2016-03-08 07:33:41', 'Apple', 'http://pbs.twimg.com/media/Cc-Cn3IVAAAMc8g.jpg'),");
            out.newLine();out.write("(" + (eventId + 1) + ", '2016-03-11 06:12:58', 'Myanmar', 'http://pbs.twimg.com/media/CdOWXJMUkAArvi6.jpg'),");
            out.newLine();out.write("(" + (eventId + 2) + ", '2016-03-08 13:43:47', 'Tunisia', 'http://pbs.twimg.com/media/Cc_BDSzWwAAB9GF.jpg'),");
            out.newLine();out.write("(" + (eventId + 3) + ", '2016-03-09 10:43:45', 'European Union', 'http://pbs.twimg.com/media/CczJPxHXIAEteRS.jpg'),");
            out.newLine();out.write("(" + (eventId + 4) + ", '2016-03-11 06:14:57', 'Pennsylvania', 'http://pbs.twimg.com/media/CdKX1EsW4AEShGf.jpg'),");
            out.newLine();out.write("(" + (eventId + 5) + ", '2016-03-07 02:43:12', 'Cristiano Ronaldo', 'http://pbs.twimg.com/media/CczOb6aW0AAu9hM.jpg'),");
            out.newLine();out.write("(" + (eventId + 6) + ", '2016-03-06 07:14:45', 'Conor McGregor', 'http://pbs.twimg.com/media/Cc19e0uXIAAcU8V.jpg'),");
            out.newLine();out.write("(" + (eventId + 7) + ", '2016-03-11 06:12:34', 'Google', 'http://pbs.twimg.com/media/CdFvf_hWIAACt2J.jpg'),");
            out.newLine();out.write("(" + (eventId + 8) + ", '2016-03-11 06:15:04', 'Liverpool ', 'http://pbs.twimg.com/media/Cc3-Yf8W8AM7bnh.jpg'),");
            out.newLine();out.write("(" + (eventId + 9) + ", '2016-03-07 04:57:51', 'Yemen', 'http://pbs.twimg.com/media/CbDUGQkWIAAt7zM.jpg'),");
            out.newLine();out.write("(" + (eventId + 10) + ", '2016-03-07 00:50:08', 'Andy Murray', 'http://pbs.twimg.com/media/Cc4kLxVWoAI6oWM.jpg'),");
            out.newLine();out.write("(" + (eventId + 11) + ", '2016-03-10 14:12:52', 'Game of Thrones', 'http://pbs.twimg.com/media/CdGbQqeUYAA3tIE.jpg'),");
            out.newLine();out.write("(" + (eventId + 12) + ", '2016-03-09 07:43:30', 'International Women''s Day', NULL),");
            out.newLine();out.write("(" + (eventId + 13) + ", '2016-03-09 22:03:11', 'Manchester United', 'http://pbs.twimg.com/media/Cc4zCRBWoAAEW_d.jpg'),");
            out.newLine();out.write("(" + (eventId + 14) + ", '2016-03-08 14:16:29', 'J. K. Rowling', 'http://pbs.twimg.com/media/CdB64O9WIAAR2OJ.jpg'),");
            out.newLine();out.write("(" + (eventId + 15) + ", '2016-03-07 00:52:40', 'India', 'http://pbs.twimg.com/media/Cc4ALNHW4AAhA2q.jpg'),");
            out.newLine();out.write("(" + (eventId + 16) + ", '2016-03-14 00:41:42', 'Angela Merkel', 'http://pbs.twimg.com/media/CdciljIXIAA5LVN.jpg'),");
            out.newLine();out.write("(" + (eventId + 17) + ", '2016-03-08 07:33:01', 'Hulk Hogan', 'http://pbs.twimg.com/media/Cc9zbzqW8AEf15u.jpg'),");
            out.newLine();out.write("(" + (eventId + 18) + ", '2016-03-14 00:42:11', 'Maria Sharapova', 'http://pbs.twimg.com/media/Cc-I1tUWAAA2QEb.jpg');");
            out.newLine();


            out.newLine();out.write("INSERT INTO `eventscategories` (`categoryId`, `eventId`) VALUES");
            out.newLine();out.write("(3, " + (eventId + 10) + "),"); // (3, 11),
            out.newLine();out.write("(2, " + (eventId + 10) + "),"); // (2, 11),
            out.newLine();out.write("(2, " + (eventId + 15) + "),"); // (2, 16),
            out.newLine();out.write("(2, " + (eventId + 5) + "),"); // (2, 6),
            out.newLine();out.write("(2, " + (eventId + 6) + "),"); // (2, 7),
            out.newLine();out.write("(3, " + (eventId + 9) + "),"); // (3, 10),
            out.newLine();out.write("(2, " + (eventId + 9) + "),"); // (2, 10),
            out.newLine();out.write("(2, " + (eventId + 0) + "),"); // (2, 1),
            out.newLine();out.write("(3, " + (eventId + 17) + "),"); // (3, 18),
            out.newLine();out.write("(2, " + (eventId + 17) + "),"); // (2, 18),
            out.newLine();out.write("(3, " + (eventId + 2) + "),"); // (3, 3),
            out.newLine();out.write("(2, " + (eventId + 2) + "),"); // (2, 3),
            out.newLine();out.write("(2, " + (eventId + 14) + "),"); // (2, 15),
            out.newLine();out.write("(2, " + (eventId + 12) + "),"); // (2, 13),
            out.newLine();out.write("(3, " + (eventId + 3) + "),"); // (3, 4),
            out.newLine();out.write("(2, " + (eventId + 3) + "),"); // (2, 4),
            out.newLine();out.write("(6, " + (eventId + 13) + "),"); // (6, 14),
            out.newLine();out.write("(3, " + (eventId + 11) + "),"); // (3, 12),
            out.newLine();out.write("(2, " + (eventId + 11) + "),"); // (2, 12),
            out.newLine();out.write("(4, " + (eventId + 7) + "),"); // (4, 8),
            out.newLine();out.write("(7, " + (eventId + 7) + "),"); // (7, 8),
            out.newLine();out.write("(2, " + (eventId + 1) + "),"); // (2, 2),
            out.newLine();out.write("(2, " + (eventId + 8) + "),"); // (2, 9),
            out.newLine();out.write("(4, " + (eventId + 4) + "),"); // (4, 5),
            out.newLine();out.write("(5, " + (eventId + 4) + "),"); // (5, 5),
            out.newLine();out.write("(7, " + (eventId + 4) + "),"); // (7, 5),
            out.newLine();out.write("(3, " + (eventId + 16) + "),"); // (3, 17),
            out.newLine();out.write("(2, " + (eventId + 16) + "),"); // (2, 17),
            out.newLine();out.write("(3, " + (eventId + 18) + "),"); // (3, 19),
            out.newLine();out.write("(2, " + (eventId + 18) + ");"); // (2, 19),
            out.newLine();


            out.newLine();out.write("INSERT INTO `eventsusers` (`userId`, `eventId`, `timestamp`, `hits`, `time`) VALUES");
            out.newLine();out.write("(" + userId + ", " + (eventId + 10) + ", '2016-03-06 18:22:42', 1, 20051),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 8) + ", '2016-03-13 19:58:49', 5, 153290),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 6) + ", '2016-03-07 22:48:46', 2, 41166),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 2) + ", '2016-03-06 19:03:15', 2, 145720),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 5) + ", '2016-03-06 18:25:55', 1, 61094),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 13) + ", '2016-03-06 21:41:18', 1, 74358),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 9) + ", '2016-03-06 23:55:51', 1, 400793),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 15) + ", '2016-03-07 04:58:08', 1, 128346),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 0) + ", '2016-03-07 21:40:33', 1, 10377),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 2) + ", '2016-03-09 02:21:07', 2, 21863),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 17) + ", '2016-03-08 07:05:08', 1, 104967),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 0) + ", '2016-03-08 07:35:44', 1, 61226),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 0) + ", '2016-03-08 13:25:34', 1, 809),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 3) + ", '2016-03-08 13:25:47', 1, 942),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 18) + ", '2016-03-14 00:44:06', 3, 139266),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 14) + ", '2016-03-08 14:26:12', 1, 7291),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 16) + ", '2016-03-09 02:20:33', 1, 27288),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 12) + ", '2016-03-09 08:33:49', 2, 11165),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 18) + ", '2016-03-09 03:28:09', 1, 2258),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 7) + ", '2016-03-13 18:02:49', 2, 148006),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 11) + ", '2016-03-13 17:31:06', 2, 6926),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 4) + ", '2016-03-13 19:58:00', 1, 850),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 1) + ", '2016-03-13 20:00:06', 1, 9837),");
            out.newLine();out.write("(" + userId + ", " + (eventId + 16) + ", '2016-03-14 00:44:11', 1, 2860);");
            out.newLine();


            for (int i = eventId; i < eventId + 19; i++) {
                out.newLine();out.write("INSERT INTO `images` (`eventId`, `url`) VALUES");
                for (int j = 0; j <= imageLimit; j++) {
                    if (j == imageLimit) {
                        out.newLine();out.write("(" + i + ", 'http://pbs.twimg.com/media/Cc5pSf0WIAUwaPW.jpg');"); // vazhno, chto na poslednei iteracii cikla v konce ne dolzhno bitj zapjatoi!!!!
                    } else {
                        out.newLine();out.write("(" + i + ", 'http://pbs.twimg.com/media/Cc5pSf0WIAUwaPW.jpg'),"); // vazhno, chto na poslednei iteracii cikla v konce ne dolzhno bitj zapjatoi!!!!
                    }
                }
            }
            out.newLine();


            for (int i = eventId; i < eventId + 19; i++) {
                out.newLine();out.write("INSERT INTO `summaries` (`eventId`, `length`, `text`, `timestamp`) VALUES ");
                for (int j = 0; j <= summaryLimit; j++) {
                    if (j == summaryLimit) {
                        out.newLine();out.write("(" + i + ", 75, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45'),");
                        out.newLine();out.write("(" + i + ", 90, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45'),");
                        out.newLine();out.write("(" + i + ", 105, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45'),");
                        out.newLine();out.write("(" + i + ", 120, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. Warplanes bombed an opposition-held town in northern Syria, killing at least 12 people, monitoring groups said, and insurgents shelled a predominantly Kurdish neighborhood in the northern city of Aleppo. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45'),");
                        out.newLine();out.write("(" + i + ", 135, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. His comments came amid violence in Syria that claimed the lives of over a dozen people. Warplanes bombed an opposition-held town in northern Syria, killing at least 12 people, monitoring groups said, and insurgents shelled a predominantly Kurdish neighborhood in the northern city of Aleppo. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45');");
                    } else {
                        out.newLine();out.write("(" + i + ", 75, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45'),");
                        out.newLine();out.write("(" + i + ", 90, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45'),");
                        out.newLine();out.write("(" + i + ", 105, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45'),");
                        out.newLine();out.write("(" + i + ", 120, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. Warplanes bombed an opposition-held town in northern Syria, killing at least 12 people, monitoring groups said, and insurgents shelled a predominantly Kurdish neighborhood in the northern city of Aleppo. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45'),");
                        out.newLine();out.write("(" + i + ", 135, ' The main Syrian opposition coalition will decide later this week on whether to take part in peace talks scheduled to resume Wednesday in Geneva, the head of the group said Monday. His comments came amid violence in Syria that claimed the lives of over a dozen people. Warplanes bombed an opposition-held town in northern Syria, killing at least 12 people, monitoring groups said, and insurgents shelled a predominantly Kurdish neighborhood in the northern city of Aleppo. Defense Ministry spokesman Maj. Gen. Igor Konashenkov said Monday that Russia \"is ready to provide all necessary help to international and foreign organizations in the delivery of humanitarian aid.\" He said this assistance will include allowing the unloading and temporary storage of aid cargos at the Russian naval base in Tartus, as well as receiving and storing aid at the air base in Hemeimeem.', '2016-03-07 19:44:45'),");
                    }
                }
            }
            out.newLine();

            for (int i = eventId; i < eventId + 19; i++) {
                out.newLine();out.write("INSERT INTO `news` (`eventId`, `title`, `url`, `logoUrl`, `timestamp`) VALUES");
                for (int j = 0; j <= newsLimit; j++) {
                    if (j == newsLimit) {
                        out.newLine();out.write("(" + i + ", 'Syrian Opposition Undecided Over Peace Talks - ABC News', 'http://abcnews.go.com/International/wireStory/russia-open-syria-bases-international-aid-cargos-37456296', '', '2016-03-07 16:12:00');");
                    } else {
                        out.newLine();out.write("(" + i + ", 'Syrian Opposition Undecided Over Peace Talks - ABC News', 'http://abcnews.go.com/International/wireStory/russia-open-syria-bases-international-aid-cargos-37456296', '', '2016-03-07 16:12:00'),");
                    }
                }
            }
            out.newLine();


            for (int i = eventId; i < eventId + 19; i++) {
                out.newLine();out.write("INSERT INTO `tweets` (`eventId`, `username`, `screenName`, `profileImgUrl`, `imageUrl`, `text`, `timestamp`, `url`) VALUES");
                for (int j = 0; j <= tweetLimit; j++) {
                    if (j == tweetLimit) {
                        out.newLine();out.write("(" + i + ", 'WHO', 'WHO', 'http://pbs.twimg.com/profile_images/2189537674/WHOLogo_bigger.png', '', '.@WHOEMRO Regional Director calls for urgent funding to support #Syria health response  https://t.co/6LNZ1JJARk', '2016-03-07 08:50:32', 'https://t.co/qwdMT7I0NU');");
                    } else {
                        out.newLine();out.write("(" + i + ", 'WHO', 'WHO', 'http://pbs.twimg.com/profile_images/2189537674/WHOLogo_bigger.png', '', '.@WHOEMRO Regional Director calls for urgent funding to support #Syria health response  https://t.co/6LNZ1JJARk', '2016-03-07 08:50:32', 'https://t.co/qwdMT7I0NU'),");
                    }
                }
            }
            out.newLine();

            eventId += 19;
        }

        try {
            out.close();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}